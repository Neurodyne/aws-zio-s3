/*
 * Copyright 2019 Branislav Lazic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.branislavlazic.aws.zio.s3

import zio.{ ZIO }
import zio.duration._
import zio.test._
import zio.test.Assertion._
import zio.test.TestAspect._

import S3._
import java.io.IOException

import Helper._

object Tests {
  val listSuite = suite("AWS list buckets")(
    testM("list") {
      println(s"Using Region: ${region} and Endpoint: ${endpoint}")
      val list = for {
        s3   <- createClient(region, endpoint).mapError(_ => new IOException("S3 client reation failed"))
        list <- listBuckets(s3)
        _    = println(list)
      } yield list

      assertM(list.foldM(_ => ZIO.fail("failed"), _ => ZIO.succeed("ok")), equalTo("ok"))
    } @@ timeout(10.seconds)
  )
}

object BaseSpec extends DefaultRunnableSpec(suite("AWS Spec")(Tests.listSuite))

object Helper {
  import scala.collection.JavaConverters._
  import java.nio.file.{ Files }
  import java.io.File

  import software.amazon.awssdk.regions.Region

  def createOutFile(dir: String = "./", file: String = "outfile"): File = {
    val outDir = Files.createTempDirectory(dir)
    val path   = outDir.resolve(file)
    println("File to create path: " + path)
    Files.createFile(path).toFile

  }

  val region: Region = Region.US_EAST_1
  val env            = System.getenv()
  val endpoint       = env.get("AWS_ENDPOINT")

}
