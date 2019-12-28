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

import java.nio.file.{ Paths }
import java.util.concurrent.CompletableFuture
import java.net.URI
import scala.collection.JavaConverters._

import zio.{ IO, Task, ZIO }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.{
  CreateBucketRequest,
  CreateBucketResponse,
  DeleteBucketRequest,
  DeleteBucketResponse,
  DeleteObjectRequest,
  DeleteObjectResponse,
  GetObjectRequest,
  GetObjectResponse,
  ListBucketsResponse,
  ListObjectsV2Request,
  ListObjectsV2Response,
  PutObjectRequest,
  PutObjectResponse,
  S3Object
}

final class AwsLink extends GenericLink {

  val service = new GenericLink.Service[Any] {
    def createClient(region: Region, endpoint: String = ""): Task[S3AsyncClient] = {
      val client =
        if (endpoint.isEmpty())
          S3AsyncClient
            .builder()
            .region(region)
            .build()
        else
          S3AsyncClient
            .builder()
            .region(region)
            .endpointOverride(URI.create(endpoint))
            .build()
      Task(client)
    }

    def createBucket(buck: String)(implicit s3: S3AsyncClient): Task[CreateBucketResponse] =
      IO.effectAsync[Throwable, CreateBucketResponse] { callback =>
        handleResponse(
          s3.createBucket(CreateBucketRequest.builder().bucket(buck).build()),
          callback
        )
      }

    def delBucket(buck: String)(implicit s3: S3AsyncClient): Task[DeleteBucketResponse] =
      IO.effectAsync[Throwable, DeleteBucketResponse] { callback =>
        handleResponse(
          s3.deleteBucket(DeleteBucketRequest.builder().bucket(buck).build()),
          callback
        )
      }

    def listBuckets(implicit s3: S3AsyncClient): Task[ListBucketsResponse] =
      IO.effectAsync[Throwable, ListBucketsResponse] { callback =>
        handleResponse(s3.listBuckets(), callback)
      }

    def listBucketObjects(buck: String)(implicit s3: S3AsyncClient): Task[ListObjectsV2Response] =
      for {
        resp <- IO.effect(s3.listObjectsV2(ListObjectsV2Request.builder().bucket(buck).build()))
        list <- IO.effectAsync[Throwable, ListObjectsV2Response] { callback =>
                 handleResponse(
                   resp,
                   callback
                 )
               }
      } yield list

    def listObjectsKeys(buck: String)(implicit s3: S3AsyncClient): Task[List[String]] =
      for {
        list <- listBucketObjects(buck)
        keys = list.contents.asScala.map(_.key).toList
      } yield keys

    def lookupObject(buck: String, key: String)(implicit s3: S3AsyncClient): Task[Boolean] =
      for {
        list <- listBucketObjects(buck)
        res  = list.contents.contains(key)
      } yield res

    def putObject(buck: String, key: String, file: String)(implicit s3: S3AsyncClient): Task[PutObjectResponse] =
      IO.effectAsync[Throwable, PutObjectResponse] { callback =>
        handleResponse(
          s3.putObject(PutObjectRequest.builder().bucket(buck).key(key).build(), Paths.get(file)),
          callback
        )
      }

    def getObject(buck: String, key: String, file: String)(implicit s3: S3AsyncClient): Task[GetObjectResponse] =
      IO.effectAsync[Throwable, GetObjectResponse] { callback =>
        handleResponse(
          s3.getObject(GetObjectRequest.builder().bucket(buck).key(key).build(), Paths.get(file)),
          callback
        )
      }

    def delObject(buck: String, key: String)(implicit s3: S3AsyncClient): Task[DeleteObjectResponse] =
      IO.effectAsync[Throwable, DeleteObjectResponse] { callback =>
        handleResponse(
          s3.deleteObject(DeleteObjectRequest.builder().bucket(buck).key(key).build()),
          callback
        )
      }

    def handleResponse[T](
      fut: CompletableFuture[T],
      callback: Task[T] => Unit
    ): Unit =
      fut.handle[Unit]((response, err) => {
        err match {
          case null => callback(IO.succeed(response))
          case ex   => callback(IO.fail(ex))
        }
      })
  }
}
