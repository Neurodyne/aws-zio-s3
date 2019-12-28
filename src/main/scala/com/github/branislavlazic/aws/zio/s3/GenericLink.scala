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

import java.util.concurrent.CompletableFuture
import java.nio.file.{ Path }

import zio.{ Task, ZIO }
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
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

trait GenericLink {
  val service: GenericLink.Service[Any]
}

object GenericLink {
  trait Service[R] {

    // Bucket API

    def createClient(region: Region, endpoint: String): Task[S3AsyncClient]
    def createBucket(buck: String)(implicit s3: S3AsyncClient): Task[CreateBucketResponse]
    def delBucket(buck: String)(implicit s3: S3AsyncClient): Task[DeleteBucketResponse]
    def listBuckets(buck: String)(implicit s3: S3AsyncClient): Task[ListBucketsResponse]

    // Object API

    def listBucketObjects(buck: String)(implicit s3: S3AsyncClient): Task[ListObjectsV2Response]
    def listObjectsKeys(buck: String)(implicit s3: S3AsyncClient): Task[List[String]]
    def lookupObject(buck: String, key: String)(implicit s3: S3AsyncClient): Task[Boolean]
    def putObject(buck: String, key: String, filePath: Path)(implicit s3: S3AsyncClient): Task[PutObjectResponse]
    def getObject(buck: String, key: String, outFile: String)(implicit s3: S3AsyncClient): Task[GetObjectResponse]
    def delObject(buck: String, key: String)(implicit s3: S3AsyncClient): Task[DeleteObjectResponse]

    def handleResponse[T](
      fut: CompletableFuture[T],
      callback: Task[T] => Unit
    ): Unit
  }
}
