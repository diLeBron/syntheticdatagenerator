package utils

import java.time.LocalDateTime

class Logger {
  def info(msg: String): Unit = {
    println(s"${LocalDateTime.now().toString} INFO ${msg}")
  }

  def warning(msg: String): Unit = {
    println(s"${LocalDateTime.now().toString} WARNING ${msg}")
  }

  def error(msg: String, e: Throwable): Unit = {
    println(
      s"${LocalDateTime.now().toString} ERROR ${msg}\nErrorMsg: ${e.getMessage}\n${e.getStackTrace
          .mkString("StackTrace(", ",", ")")}"
    )
  }
}
