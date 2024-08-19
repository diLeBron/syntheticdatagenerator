package ru.digitalleague.syntheticdatagenerator

//import cats.effect.{ExitCode, IO, IOApp}
//
//object IOAppTemplate extends IOApp {
//
//  def run(args: List[String]): IO[ExitCode] =
//    args.headOption match {
//      case Some(params) =>
//        IO(println(s"${params.split(',').mkString("Array(", ", ", ")")}")).as(ExitCode.Success)
//      case None =>
//        IO(System.err.println("Input parameters was not specified.")).as(ExitCode(2))
//    }
//}
