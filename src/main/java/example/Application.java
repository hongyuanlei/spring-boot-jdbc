package example;

import example.entity.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class Application implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        initDatabase();
        preparingData();
        doQuery();
    }

    private void doQuery() {
        LOGGER.info("Querying for customer records where first_name = 'Josh':");
        String querySql = "SELECT id, first_name, last_name FROM customers WHERE first_name = ?";
        jdbcTemplate.query(
                querySql,
                new Object[] { "Josh" },
                (rs, rowNum) -> new Customer(

                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"))
        ).forEach(customer ->
                LOGGER.info(customer.toString()));
    }

    private void preparingData() {
        List<Object[]> splitUpNames = Arrays.asList(
                "John Woo",
                "Jeff Dean",
                "Josh Bloch",
                "Josh Long")
                .stream()
                .map(name -> name.split(" "))
                .collect(Collectors.toList());

        splitUpNames.forEach(name ->
                LOGGER.info(String.format(
                        "Inserting customer record for %s %s", name[0], name[1])));

        String insertSql = "INSERT INTO customers(first_name, last_name) VALUES (?,?)";
        jdbcTemplate.batchUpdate(insertSql, splitUpNames);
    }

    private void initDatabase() {
        LOGGER.info("Creating tables");

        jdbcTemplate.execute("DROP TABLE IF EXISTS customers");
        jdbcTemplate.execute("CREATE TABLE customers(id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255))");
    }
}
