package com.neutrinosys.peopledb.repository;
import com.neutrinosys.peopledb.exception.UnableToSaveException;
import com.neutrinosys.peopledb.model.Person;

import java.math.BigDecimal;
import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class PeopleRepository {

    public static final String SAVE_PERSON_SQL = "INSERT INTO PEOPLE (FIRST_NAME, LAST_NAME, DOB) VALUES(?, ?, ?)";
    public static final String FIND_BY_ID_SQL = "SELECT ID, FIRST_NAME, LAST_NAME, DOB , SALARY FROM PEOPLE WHERE ID = ? ";
    private  Connection connection;
    public PeopleRepository(Connection connection) {

        this.connection = connection;
    }
    public Person save(Person person)  throws UnableToSaveException{
        try {
            PreparedStatement ps = connection.prepareStatement(SAVE_PERSON_SQL,Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, person.getFirstname());
            ps.setString(2, person.getLastName());
            ps.setTimestamp(3, convertDobToTimestamp(person.getDob()));
            int recordsAffected = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()){

                long id = rs.getLong(1);
                person.setId(id);
                System.out.println(person);
            }
            System.out.printf("Records affected: %d%n", recordsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new UnableToSaveException("Tried to save person: " + person);
        }
        return person;
    }


    public Optional<Person> findById(Long id) {
      Person person = null;

        try {
            PreparedStatement ps = connection.prepareStatement(FIND_BY_ID_SQL);
            ps.setLong(1,id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){

                long personId = rs.getLong("ID");
                String firstName = rs.getString("FIRST_NAME");
                String lastName = rs.getString("LAST_NAME");
                ZonedDateTime dob = ZonedDateTime.of(rs.getTimestamp("DOB").toLocalDateTime(), ZoneId.of("+0"));
                BigDecimal salary = rs.getBigDecimal("SALARY");
                person = new Person(personId,firstName, lastName,dob,salary);
                person.setId(personId);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(person);
    }
    public void delete(Person person) {
        try {
           PreparedStatement ps = connection.prepareStatement("DELETE FROM PEOPLE WHERE ID =?");
        ps.setLong(1,person.getId());
            int affectedRecordCount = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void delete (Person...people){
        try {
            Statement stmt = connection.createStatement();
            String ids = Arrays.stream(people).map(Person::getId).map(String::valueOf).collect(joining(","));
            int affectedRecordCount = stmt.executeUpdate("DELETE FROM PEOPLE WHERE ID IN (:ids)".replace(":ids", ids));
            System.out.println(affectedRecordCount);
        } catch (SQLException e) {
            e.printStackTrace();

        }

    }

    public long count() {

        long records = 0;
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM PEOPLE");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                records = rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public void update(Person person) {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE PEOPLE SET FIRST_NAME=?, LAST_NAME=?,  DOB=?, SALARY=? WHERE ID =?");
            ps.setString(1, person.getFirstname());
            ps.setString(2, person.getFirstname() );
            ps.setTimestamp(3,convertDobToTimestamp(person.getDob()));
            ps.setBigDecimal(4,person.getSalary());
            ps.setLong(5, person.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();        }
    }

    private static Timestamp convertDobToTimestamp(ZonedDateTime dob) {
        return Timestamp.valueOf(dob.withZoneSameInstant(ZoneId.of("+0")).toLocalDateTime());
    }

}
