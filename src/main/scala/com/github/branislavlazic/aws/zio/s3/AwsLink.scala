package com.github.branislavlazic.aws.zio.s3

import java.nio.file.{ Path, Paths }
import java.util.concurrent.CompletableFuture
import java.net.URI

import zio.{ Task, ZIO }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient

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

  }
}
