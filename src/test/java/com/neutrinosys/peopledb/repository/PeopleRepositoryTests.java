package com.neutrinosys.peopledb.repository;

import com.neutrinosys.peopledb.model.Address;
import com.neutrinosys.peopledb.model.Person;
import com.neutrinosys.peopledb.model.Region;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

public class PeopleRepositoryTests {

   Connection connection;
    private PeopleRepository repo;


    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager .getConnection("jdbc:h2:C:/Users/jogaq/dbtest");
        connection.setAutoCommit(false);
        repo = new PeopleRepository(connection);
    }

    @AfterEach
    void tearDown() throws SQLException{
        if (connection != null) {
            connection.close();
        }
    }

    @Test
    public void canSaveOnePerson() throws SQLException {
        Person jonh = new Person("John", "Smith", ZonedDateTime.of(1980,11,15,15,15,0,0, ZoneId.of("-6")));
        Person savedPerson = repo.save(jonh);
        assertThat(savedPerson.getId()).isGreaterThan(0);




    }

    @Test

    public void canSaveTwoPeople() throws SQLException {


        Person jonh = new Person("Javier", "Smith", ZonedDateTime.of(1980,11,15,15,15,0,0, ZoneId.of("-6")));
        Person Bobby = new Person("Javier", "Smith", ZonedDateTime.of(1980,11,15,15,15,0,0, ZoneId.of("-6")));
        Person savedPerson1 = repo.save(jonh);
        Person savedPerson2 = repo.save(Bobby);
        assertThat(savedPerson1.getId()).isNotEqualTo(savedPerson2.getId());

    }

    @Test
    public void canSavePersonWithAddress(){

        Person jonh = new Person("Javier", "Smith", ZonedDateTime.of(1980,11,15,15,15,0,0, ZoneId.of("-6")));
        Address address = new Address(null, "123 Beale St.", "Apt. 1A", "Wala Wala", "WA","90210","United States","Fulton County", Region.WEST);
        jonh.setHomeAddress(address);

        Person savedPerson = repo.save(jonh);
        assertThat(savedPerson.getHomeAddress().get().id()).isGreaterThan(0);
    }

    @Test
    public void canFindPersonById(){

        Person savedPerson = repo.save(new Person("test", "jackson", ZonedDateTime.now()));
        Person foundPerson = repo.findById(savedPerson.getId()).get();
        assertThat(foundPerson).isEqualTo(savedPerson);
    }

    @Test
    public void testPersonIdNotFound(){

        Optional<Person> foundPerson = repo.findById(-1L);
        assertThat(foundPerson).isEmpty();
    }

    public void canGetCount()  {

        long startCount = repo.count();
       repo.save(new Person("test", "jackson", ZonedDateTime.now()));
       repo.save(new Person("test", "jackson", ZonedDateTime.now()));
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount + 2);

    }

    @Test
    public void canDelete(){
        long startCount = repo.count();
        System.out.println(startCount);
        Person savedPerson = repo.save(new Person("test", "jackson", ZonedDateTime.now()));
         startCount = repo.count();
        System.out.println(startCount);
        repo.delete(savedPerson);
        long endCount = repo.count();
        System.out.println(endCount);
        assertThat(endCount).isEqualTo(startCount - 1);


    }

    @Test
    public void canDeleteMultiplePeople() {

        Person p1 = repo.save(new Person("test", "jackson", ZonedDateTime.now()));
        Person p2 = repo.save(new Person("test", "jackson", ZonedDateTime.now()));
        long startCount = repo.count();
        repo.delete(p1,p2);
        long endCount = repo.count();
        assertThat(endCount).isEqualTo(startCount - 2);
    }

    @Test
    public void experiment (){

        Person p1 = new Person(10L,null,null,null);
        Person p2 = new Person(20L,null,null,null);
        Person p3 = new Person(30L,null,null,null);
        Person p4 = new Person(40L,null,null,null);
        Person p5 = new Person(50L,null,null,null);

        Person[] people = Arrays.asList(p1, p2, p3, p4, p5).toArray(new Person[]{});
        String ids = Arrays.stream(people).map(Person::getId).map(String::valueOf).collect(joining(","));
        System.out.println(ids);
    }

    @Test
    public void canUpdate(){

        Person savedPerson = repo.save(new Person("test", "jackson", ZonedDateTime.now()));
        Person p1 = repo.findById(savedPerson.getId()).get();
        savedPerson.setSalary(new BigDecimal("73000.28"));
        repo.update(savedPerson);
        Person p2 = repo.findById(savedPerson.getId()).get();
        assertThat(p2.getSalary()).isNotEqualTo(p1.getSalary());


    }


    @Test
    @Disabled
    public void loadData() throws IOException, SQLException {

        Files.lines(Path.of("C:/Users/jogaq/Desktop/Hr5m.csv"))
                .skip(1)
                .limit(1)
                .map(l -> l.split(","))
                .map(a -> {
                    LocalDate dob = LocalDate.parse(a[10], DateTimeFormatter.ofPattern("M/d/yyyy"));
                    LocalTime tob = LocalTime.parse(a[11], DateTimeFormatter.ofPattern("hh:mm:ss a"));
                    LocalDateTime dtob = LocalDateTime.of(dob, tob);
                    ZonedDateTime zdtob = ZonedDateTime.of(dtob, ZoneId.of("+0"));
                    Person person = new Person(a[2], a[4], zdtob);
                    person.setSalary(new BigDecimal(a[25]));
                    person.setEmail(a[6]);
                    return person ;

                })
                .forEach(repo::save); //lo mismo a poner p -> repo.save(p)
                connection.commit();



    }


}
