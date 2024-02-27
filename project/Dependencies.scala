import sbt.*

object Dependencies {
  val slf4jVersion = "2.0.9"
  val AkkaVersion = "2.8.5"
  val AkkaHttpVersion = "10.5.3"
  val SlickVersion = "3.4.1"
  val AkkaProjectionVersion = "1.2.4"
  val LogbackVersion = "1.5.0"
  val AlpakkaKafkaVersion = "4.0.2"
  val ChimneyVersion = "0.8.5"
  val PostgresJdbcVersion = "42.7.1"
  val FlywayVersion = "10.8.1"
  val CorsHandlerVersion = "1.2.0"
  val SwaggerVersion = "2.11.0"
  val PureConfigVersion = "0.17.5"

  // rest
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
  val sprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion
  val akkaActorTyped = "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
  val corsHandler = "ch.megard" %% "akka-http-cors" % CorsHandlerVersion
  val swagger = "com.github.swagger-akka-http" %% "swagger-akka-http" % SwaggerVersion

  // reactive streams
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % AkkaVersion
  val akkaStreamTyped = "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion

  // logger
  val logback = "ch.qos.logback" % "logback-classic" % LogbackVersion

  // kafka connector
  val alpakkaKafka = "com.typesafe.akka" %% "akka-stream-kafka" % AlpakkaKafkaVersion

  // converting to dto and back to entity
  val chimney = "io.scalaland" %% "chimney" % ChimneyVersion

  // db migration
  val flyway = "org.flywaydb" % "flyway-core" % "10.8.1"

  // db mapping
  val slick = "com.typesafe.slick" %% "slick" % SlickVersion
  val slickHikariCP = "com.typesafe.slick" %% "slick-hikaricp" % SlickVersion
  val postgresqlJdbc = "org.postgresql" % "postgresql" % PostgresJdbcVersion

  // hocon config mapping
  val pureConfig = "com.github.pureconfig" %% "pureconfig" % PureConfigVersion

  val commonDependencies: Seq[ModuleID] = Seq(
      akkaHttp
    , sprayJson
    , akkaActorTyped
    , corsHandler
    , swagger
    , akkaStream
    , akkaStreamTyped
    , logback
    , alpakkaKafka
    , chimney
    , flyway
    , slick
    , slickHikariCP
    , postgresqlJdbc
    , pureConfig
  )
}
