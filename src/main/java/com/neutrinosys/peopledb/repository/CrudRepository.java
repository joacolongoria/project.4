package com.neutrinosys.peopledb.repository;

import com.neutrinosys.peopledb.annotation.Id;
import com.neutrinosys.peopledb.annotation.MultiSQL;
import com.neutrinosys.peopledb.annotation.SQL;
import com.neutrinosys.peopledb.exception.UnableToSaveException;
import com.neutrinosys.peopledb.model.CrudOperation;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

abstract class CrudRepository<T>{
    protected Connection connection;

    public CrudRepository(Connection connection) {
        this.connection = connection;
    }

    private String getSqlByAnnotation(CrudOperation operationType, Supplier<String> sqlGetter){
        Stream<SQL> multiSqlStream = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(MultiSQL.class))
                .map(m -> m.getAnnotation(MultiSQL.class))
                .flatMap(msql -> Arrays.stream(msql.value()));

        Stream<SQL> sqlStream = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(SQL.class))
                .map(m -> m.getAnnotation(SQL.class));

        return Stream.concat(multiSqlStream,sqlStream)
               .filter(a -> a.operationType().equals(operationType))
                .map(SQL::value)
                .findFirst().orElseGet(sqlGetter);


    }

    public T save(T entity)  throws UnableToSaveException {
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.SAVE, this::getSaveSql), Statement.RETURN_GENERATED_KEYS);
            mapForSave(entity, ps);
            int recordsAffected = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()){

                long id = rs.getLong(1);
                setIdByAnnotation(id,entity);
              //  System.out.println(entity);
            }
           // System.out.printf("Records affected: %d%n", recordsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new UnableToSaveException("Tried to save entity: " + entity);
        }
        return entity;
    }
    public Optional<T> findById(Long id) {
        T entity = null;

        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.FIND_BY_ID,this::getFindByIdSql));
            ps.setLong(1,id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){

                entity = extractEntityFromResultSet(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(entity);

    }

    public List<T> findAll(){
        List<T> entities = new ArrayList<>();
        try{

            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.FIND_ALL,this::getFindAllSql));
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                entities.add(extractEntityFromResultSet(rs));
            }
        }catch (SQLException e){
            e.printStackTrace();
        }

        return entities;
    }


    public long count() {

        long records = 0;
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.COUNT,this::getCountSql));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                records = rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public void delete(T entity) {
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.DELETE_ONE,this::getDeleteSql));
            ps.setLong(1, getIdByAnnotation(entity));
            int affectedRecordCount = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setIdByAnnotation(Long id,T entity) {

        Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .forEach(f -> {
                    f.setAccessible(true);
                    try {
                        f.set(entity, id);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Unabled to set Id field value");
                    }
                });
    }


    private Long getIdByAnnotation(T entity){

        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .map(f -> {
                    f.setAccessible(true);
                    Long id = null;
                    try {
                       id =  (long)f.get(entity);
                    } catch (IllegalAccessException e) {
                      e.printStackTrace();
                    }
                    return id;
                })
                .findFirst().orElseThrow(()-> new RuntimeException("No Id annotated field found"));
    }

    public void delete (T...entities){
        try {
            Statement stmt = connection.createStatement();
            String ids = Arrays.stream(entities).map(e -> getIdByAnnotation(e)).map(String::valueOf).collect(joining(","));
            int affectedRecordCount = stmt.executeUpdate(getSqlByAnnotation(CrudOperation.DELETE_MANY,this::getDeleteInSql).replace(":ids", ids));
            System.out.println(affectedRecordCount);
        } catch (SQLException e) {
            e.printStackTrace();

        }

    }

    public void update(T entity) {
        try {
            PreparedStatement ps = connection.prepareStatement(getSqlByAnnotation(CrudOperation.UPDATE,this::getUpdateSql));
            mapForUpdate(entity, ps);
            ps.setLong(5, getIdByAnnotation(entity));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();        }
    }




    abstract  void mapForSave(T entity, PreparedStatement ps) throws SQLException ;
    abstract void mapForUpdate(T entity, PreparedStatement ps) throws SQLException ;



    protected String getSaveSql(){throw new RuntimeException("SQL not defined.");}
    protected  String getFindByIdSql(){throw new RuntimeException("SQL not defined.");};
    protected  String getUpdateSql(){throw new RuntimeException("SQL not defined.");}
    protected  String getFindAllSql(){throw new RuntimeException("SQL not defined.");};
    protected  String getCountSql(){throw new RuntimeException("SQL not defined.");};
    protected  String getDeleteSql(){throw new RuntimeException("SQL not defined.");};
    protected  String getDeleteInSql(){throw new RuntimeException("SQL not defined.");};

    abstract T extractEntityFromResultSet(ResultSet rs) throws SQLException;


    /**
     *
     * @return rETURNS A sTRING THAR REPRESENTS THE sql NEEDED TO RETRIEVE ONE ENTITY.
     * The sql must contain one SQL parameter, i.e "?"
     * that will bind to the entity´s ID.
     */

















}
