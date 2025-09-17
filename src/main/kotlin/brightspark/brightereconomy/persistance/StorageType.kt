package brightspark.brightereconomy.persistance

import brightspark.brightereconomy.BrighterEconomy
import com.mysql.cj.jdbc.MysqlDataSource
import org.sqlite.SQLiteDataSource
import javax.sql.DataSource

@Suppress("unused")
enum class StorageType(val dataSource: () -> DataSource) {
	SQLITE({
		SQLiteDataSource().apply {
			url = "jdbc:sqlite:${BrighterEconomy.CONFIG.dbUrl()}"
		}
	}),
	MYSQL({
		MysqlDataSource().apply {
			setUrl("jdbc:mysql:${BrighterEconomy.CONFIG.dbUrl()}")
			user = BrighterEconomy.CONFIG.dbUsername()
			password = BrighterEconomy.CONFIG.dbPassword()
		}
	})
}
