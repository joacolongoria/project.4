package com.neutrinosys.peopledb.repository;

import com.neutrinosys.peopledb.exception.UnableToSaveException;
import com.neutrinosys.peopledb.model.Entity;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

abstract class CRUDRepository <T extends Entity>{
    protected Connection connection;

    public CRUDRepository(Connection connection) {
        this.connection = connection;
    }

    public T save(T entity)  throws UnableToSaveException {
        try {
            PreparedStatement ps = connection.prepareStatement(getSaveSql(), Statement.RETURN_GENERATED_KEYS);
            mapForSave(entity, ps);
            int recordsAffected = ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()){

                long id = rs.getLong(1);
                entity.setId(id);
                System.out.println(entity);
            }
            System.out.printf("Records affected: %d%n", recordsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new UnableToSaveException("Tried to save entity: " + entity);
        }
        return entity;
    }
    public Optional<T> findById(Long id) {
        T entity = null;

        try {
            PreparedStatement ps = connection.prepareStatement(getFindByIdSql());
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

            PreparedStatement ps = connection.prepareStatement(getFindAllSql());
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
            PreparedStatement ps = connection.prepareStatement(getCountSql());
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
            PreparedStatement ps = connection.prepareStatement(getDeleteSql());
            ps.setLong(1,entity.getId());
            int affectedRecordCount = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete (T...entities){
        try {
            Statement stmt = connection.createStatement();
            String ids = Arrays.stream(entities).map(T::getId).map(String::valueOf).collect(joining(","));
            int affectedRecordCount = stmt.executeUpdate(getDeleteInSql().replace(":ids", ids));
            System.out.println(affectedRecordCount);
        } catch (SQLException e) {
            e.printStackTrace();

        }

    }

    public void update(T entity) {
        try {
            PreparedStatement ps = connection.prepareStatement(getUpdateSql());
            mapForUpdate(entity, ps);
            ps.setLong(5, entity.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();        }
    }

    protected abstract String getUpdateSql();


    abstract  void mapForSave(T entity, PreparedStatement ps) throws SQLException ;
    abstract void mapForUpdate(T entity, PreparedStatement ps) throws SQLException ;
    abstract String getSaveSql();

    abstract T extractEntityFromResultSet(ResultSet rs) throws SQLException;

    /**
     *
     * @return rETURNS A sTRING THAR REPRESENTS THE sql NEEDED TO RETRIEVE ONE ENTITY.
     * The sql must contain one SQL parameter, i.e "?"
     * that will bind to the entity´s ID.
     */

    protected abstract String getFindByIdSql();
    protected abstract String getFindAllSql();
    protected abstract String getCountSql();
    protected abstract String getDeleteSql();
    protected abstract String getDeleteInSql();




















}
