package com.github.branislavlazic.aws.zio.s3

import java.nio.file.{ Path, Paths }
import java.util.concurrent.CompletableFuture
import java.net.URI
import scala.collection.JavaConverters._

import zio.{ IO, Task, ZIO }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
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
  PutObjectResponse
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

    def createBucket(name: String)(implicit client: S3AsyncClient): Task[CreateBucketResponse] =
      IO.effectAsync[Throwable, CreateBucketResponse] { callback =>
        handleResponse(
          client
            .createBucket(CreateBucketRequest.builder().bucket(name).build()),
          callback
        )
      }

    def listBuckets(name: String)(implicit client: S3AsyncClient): Task[ListBucketsResponse] =
      IO.effectAsync[Throwable, ListBucketsResponse] { callback =>
        handleResponse(client.listBuckets(), callback)
      }

    def listBucketObjects(name: String)(implicit client: S3AsyncClient): Task[ListObjectsV2Response] =
      for {
        resp <- IO.effect(client.listObjectsV2(ListObjectsV2Request.builder().bucket(name).build()))
        list <- IO.effectAsync[Throwable, ListObjectsV2Response] { callback =>
                 handleResponse(
                   resp,
                   callback
                 )
               }
      } yield list

    def listBucketObjectsKeys(name: String)(implicit client: S3AsyncClient): Task[List[String]] = {
      val list = listBucketObjects(name)
      list.map(_.contents.asScala.map(_.key).toList)
    }

    def handleResponse[T](
      completableFuture: CompletableFuture[T],
      callback: ZIO[Any, Throwable, T] => Unit
    ): Unit =
      completableFuture.handle[Unit]((response, err) => {
        err match {
          case null => callback(IO.succeed(response))
          case ex   => callback(IO.fail(ex))
        }
      })
  }
}
