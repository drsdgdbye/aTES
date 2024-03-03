package application.migration

import org.flywaydb.core.Flyway

import javax.sql.DataSource

object PostgresMigration {
  def migrate(ds: DataSource): Unit = {
    Flyway.configure()
      .dataSource(ds)
      .loggers("slf4j")
      .baselineOnMigrate(true)
      .load()
      .migrate()
  }
}
