package com.github.branislavlazic.aws.zio.s3

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
  PutObjectResponse
}

trait GenericLink {
  val service: GenericLink.Service[Any]
}

object GenericLink {
  trait Service[R] {
    def createClient(region: Region, endpoint: String): Task[S3AsyncClient]
    // def createBucket(name: String)(implicit client: S3AsyncClient): Task[CreateBucketResponse]
    // def listBuckets(name: String)(implicit client: S3AsyncClient): Task[ListBucketsResponse]
    // def listKeys(name: String)(implicit client: S3AsyncClient): Task[List[String]]
  }
}
