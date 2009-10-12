package org.taoyag.autotest

import java.io._

import org.specs._
import org.specs.runner.{ConsoleRunner, JUnit4}

object AutoTestSpec extends Specification {

  "getTestClassName" should {
    "scala-autotest/src/main/scala/AutoTest.scala" in {
      val f = new Modified(new File("scala-autotest/src/main/scala/AutoTest.scala"))
      AutoTest.getTestClassName(f) must_== "AutoTestTest"
    }
    "scala-autotest/src/test/scala/AutoTestSpec.scala" in {
      val f = new Modified(new File("scala-autotest/src/test/scala/AutoTestSpec.scala"))
      AutoTest.getTestClassName(f) must_== "AutoTestTest"
    }
    "scala-autotest/src/test/scala/AutoTestTest.scala" in {
      val f = new Modified(new File("scala-autotest/src/test/scala/AutoTestTest.scala"))
      AutoTest.getTestClassName(f) must_== "AutoTestTest"
    }
  }
}
class AutoTestTest extends JUnit4(AutoTestSpec)
object MySpecRunner extends ConsoleRunner(AutoTestSpec)
