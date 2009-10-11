package org.taoyag.autotest

import java.io._

import scala.actors.Actor._
import scala.collection.mutable.ListBuffer
import scala.io._

object AutoTest {

  def main(args: Array[String]) {
    val files = getFiles("src/main/scala") ++ getFiles("src/test/scala")
    files.foreach(observe)
  }

  def test(name: String) {
    val output = Command.exec("mvn test -Dtest=%s".format(name))
    println(output)
    //  Results:
    //
    //  Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
    val pattern = """Results.*\n(?s:.*\n)+Tests run:\s+(\d+),\sFailures:\s+(\d+),\s+Errors:\s+(\d+)""".r
    pattern.findFirstMatchIn(output) match {
      case Some(m) =>
        val run     = m.group(1).toInt
        val failure = m.group(2).toInt
        val error   = m.group(3).toInt
        if (run > 0) {
          if (failure > 0) {
            notify("autotest", "failure." + failure, 2)
          } else if (error > 0) {
            notify("autotest", "error." + error, 2)
          } else {
            notify("autotest", "success.", 0)
          }
        }
      case _ => ;
    }
  } 

  def notify(title:String, msg: String, priority: Int) {
    val cmd = "growlnotify -n autotest -p %s -m \"%s\" -t \"%s\"".format(priority, msg, title)
    Command.exec(cmd)
  }

  def observe(src: Modified) {
    actor {
      loop {
        if (src.changed) {
          val name = getTestClassName(src)

          println("start testing... " + name)
          test(name)
          src.update
        }
        Thread.sleep(1000)
      }
    }
  }

  def getTestClassName(src: Modified): String = 
    if (src.file.getPath.contains("src/main")) {
      src.file.getName.replace(".scala", "Test")
    } else {
      val n = src.file.getName.replace(".scala", "")
        if (n.endsWith("Spec")) {
        n.replace("Spec", "Test")
      } else {
        n
      }
    }

  def getFiles(dir: String):Seq[Modified] = {
    new File(dir).listFiles.flatMap(f => f match {
      case d: File if f.isDirectory => 
        getFiles(d.getPath)
      case s: File if f.getName.endsWith(".scala") => 
        List(new Modified(s))
      case _ => 
        List()
    })
  }
}

class Modified(val file: File) {
  var lastModified = file.lastModified

  def changed:Boolean = lastModified < file.lastModified

  def update {
    lastModified = file.lastModified
  }
}

object Command {
  def exec(cmd: String): String = {
    val pb = new ProcessBuilder(cmd.split("\\s"):_*)
    pb.redirectErrorStream(true)

    val p = pb.start
    val stdout = Source.fromInputStream(p.getInputStream)
    val output = new StringBuilder
    actor {
      for (line <- stdout.getLines) {
        output.append(line)
      }
    }
    val ret = p.waitFor
    output.toString
  }
}
