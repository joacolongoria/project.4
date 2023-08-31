package com.neutrinosys.peopledb.model;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Person {


    private BigDecimal salary = new BigDecimal("0");
    private Long id ;
    private String firstname;
    private String lastName;

    private ZonedDateTime dob;



    public ZonedDateTime getDob() {
        return dob;
    }

    public void setDob(ZonedDateTime dob) {
        this.dob = dob;
    }

    public Person(long id,String firstName, String lastName, ZonedDateTime dob, BigDecimal salary) {
        this(id, firstName, lastName, dob);//para reusar otro constructor se pone asi y va ahast aariiba y es par ano escribir todo con this.
        this.salary = salary;


    }


    public Person(Long id,String firtName, String lastName, ZonedDateTime dob) {

        this(firtName,lastName,dob);
        this.id = id;
    }

    public Person(String firtName, String lastName, ZonedDateTime dob) {

        this.firstname = firtName;
        this.lastName = lastName;
        this.dob = dob;
    }


    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dob=" + dob +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id) && firstname.equals(person.firstname) && lastName.equals(person.lastName) &&
                dob.withZoneSameInstant(ZoneId.of("+0")).truncatedTo(ChronoUnit.SECONDS)
                        .equals(person.dob.withZoneSameInstant(ZoneId.of("+0")).truncatedTo(ChronoUnit.SECONDS));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id,firstname, lastName, dob);
    }
}
