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

import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.net.URI

import zio.{ IO, Task, ZIO }
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.{
  CreateBucketRequest,
  CreateBucketResponse,
  DeleteBucketRequest,
  DeleteBucketResponse,
  DeleteObjectRequest,
  DeleteObjectResponse,
  ListBucketsResponse,
  ListObjectsV2Request,
  ListObjectsV2Response,
  PutObjectRequest,
  PutObjectResponse
}

import scala.concurrent.ExecutionContext

// import scala.concurrent.Future
// import scala.compat.java8.FutureConverters._
// import scala.concurrent.java8.FuturesConvertersImpl.P
import software.amazon.awssdk.services.s3.model.S3Object

// import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

object S3 {

  // def updFuture[T](fut: CompletableFuture[T])(implicit ec: ExecutionContext) = fut.toScala

  /**
   * Create an async S3 client.
   *
   * @param region                 - The AWS region
   */
  def createClient(
    region: Region,
    endpoint: String = ""
  ): Task[S3AsyncClient] = {
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

  /**
   * Create S3 bucket with the given name.
   *
   * @param s3AsyncClient - the client for async access to S3
   * @param name          - the name of the bucket
   */
  def createBucket(s3AsyncClient: S3AsyncClient, name: String): Task[CreateBucketResponse] =
    IO.effectAsync[Throwable, CreateBucketResponse] { callback =>
      handleResponse(
        s3AsyncClient
          .createBucket(CreateBucketRequest.builder().bucket(name).build()),
        callback
      )
    }

  /**
   * Delete the bucket with the given name.
   *
   * @param s3AsyncClient - the client for async access to S3
   * @param name          - the name of the bucket
   */
  def deleteBucket(s3AsyncClient: S3AsyncClient, name: String): Task[DeleteBucketResponse] =
    IO.effectAsync[Throwable, DeleteBucketResponse] { callback =>
      handleResponse(
        s3AsyncClient
          .deleteBucket(DeleteBucketRequest.builder().bucket(name).build()),
        callback
      )
    }

  /**
   * List the bucket objects.
   *
   * @param s3AsyncClient - the client for async access to S3
   * @param name          - the name of the bucket
   */
  def listBucket(
    s3AsyncClient: S3AsyncClient,
    bucketName: String
  ): Task[ListObjectsV2Response] =
    for {
      resp <- IO.effect(s3AsyncClient.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build()))
      list <- IO.effectAsync[Throwable, ListObjectsV2Response] { callback =>
               handleResponse(
                 resp,
                 callback
               )
             }
    } yield list

  def listBucket0(
    s3AsyncClient: S3AsyncClient,
    bucketName: String
  ): Task[List[S3Object]] =
    for {
      resp <- IO.effect(s3AsyncClient.listObjectsV2(ListObjectsV2Request.builder().bucket(bucketName).build()))
      list <- IO.effectAsync[Throwable, ListObjectsV2Response] { callback =>
               handleResponse(
                 resp,
                 callback
               )
             }
      out = list.contents
    } yield out.asScala.toList

  /**
   * Upload an object with a given key on S3 bucket.
   *
   * @param s3AsyncClient - the client for async access to S3
   * @param bucketName    - the name of the bucket
   * @param key           - object key
   * @param filePath      - file path
   */
  def putObject(
    s3AsyncClient: S3AsyncClient,
    bucketName: String,
    key: String,
    filePath: Path
  ): Task[PutObjectResponse] =
    IO.effectAsync[Throwable, PutObjectResponse] { callback =>
      handleResponse(
        s3AsyncClient
          .putObject(PutObjectRequest.builder().bucket(bucketName).key(key).build(), filePath),
        callback
      )
    }

  /**
   * Delete an object with a given key on S3 bucket.
   *
   * @param s3AsyncClient - the client for async access to S3
   * @param bucketName    - the name of the bucket
   * @param key           - object key
   */
  def deleteObject(
    s3AsyncClient: S3AsyncClient,
    bucketName: String,
    key: String
  ): Task[DeleteObjectResponse] =
    IO.effectAsync[Throwable, DeleteObjectResponse] { callback =>
      handleResponse(
        s3AsyncClient.deleteObject(
          DeleteObjectRequest.builder().bucket(bucketName).key(key).build()
        ),
        callback
      )
    }

  /**
   * Obtain a list of all buckets owned by the authenticated sender.
   *
   * @param s3AsyncClient - the client for async access to S3
   */
  def listBuckets(s3AsyncClient: S3AsyncClient): Task[ListBucketsResponse] =
    IO.effectAsync[Throwable, ListBucketsResponse] { callback =>
      handleResponse(s3AsyncClient.listBuckets(), callback)
    }

  private def handleResponse[T](
    completableFuture: CompletableFuture[T],
    callback: ZIO[Any, Throwable, T] => Unit
  ) =
    completableFuture.handle[Unit]((response, err) => {
      err match {
        case null => callback(IO.succeed(response))
        case ex   => callback(IO.fail(ex))
      }
    })
}
