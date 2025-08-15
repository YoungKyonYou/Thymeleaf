package com.example.thymeleaf.repository.impl;

import com.example.thymeleaf.common.PageData;
import com.example.thymeleaf.dto.UserDto;
import com.example.thymeleaf.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private static final Map<String, String> SORT = Map.of(
            "id", "id", "username", "username", "email", "email",
            "firstName", "first_name", "lastName", "last_name", "createdAt", "created_at", "phone", "phone"
    );

    private static final RowMapper<UserDto> MAPPER = (rs, rowNum) -> new UserDto(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("email"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("gender"),
            rs.getString("phone"),
            rs.getObject("birth_date", LocalDate.class),
            rs.getObject("created_at", java.time.LocalDateTime.class)
    );

    private void searchCondition(MapSqlParameterSource params, String input, String type,
                                 StringBuilder where) {

        String like = " and %s like :like";
        params.addValue("like", "%" + input.toLowerCase() + "%");

        switch (type) {
            case "username":
                like = String.format(like, "username");
                break;
            case "email":
                like = String.format(like, "email");
                break;
            case "first":
                like = String.format(like, "first_name");
                break;
            case "last":
                like = String.format(like, "last_name");
                break;
            case "phone":
                like = String.format(like, "phone");
                break;

        }

        where.append(like);


    }

    @Override
    public PageData<UserDto> search(String q, String by, int page, int size, String sort, String dir) {
        final boolean empty = (q == null || q.isBlank());
        final String qTrim = empty ? "" : q.trim();

        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder where = new StringBuilder(" where 1=1 ");

        //search 조건
        if(!empty)
            searchCondition(params, qTrim, by, where);

        long total = jdbc.queryForObject("SELECT COUNT(*) FROM users" + where, params, Long.class);

        //정렬
        String orderBy = SORT.getOrDefault(sort, "id");
        String direction = "desc".equalsIgnoreCase(dir) ? "DESC" : "ASC";
        int offset = Math.max(page, 0) * size;

        params.addValue("size", size).addValue("offset", offset);
        String sql = "SELECT * FROM users" + where + " ORDER BY " + orderBy + " " + direction +
                " LIMIT :size OFFSET :offset";

        List<UserDto> content = jdbc.query(sql, params, MAPPER);
        return new PageData<>(content, page, size, total);
    }

    @Override
    public UserDto findById(Long id) {
        return jdbc.queryForObject("SELECT * FROM users WHERE id=:id",
                new MapSqlParameterSource("id", id), MAPPER);
    }

    @Override
    public UserDto insert(UserDto u) {
        String sql = "INSERT INTO users (username,password,email,first_name,last_name,gender,birth_date,phone) " +
                "VALUES (:username,:password,:email,:firstName,:lastName,:gender,:birthDate,:phone)";

        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("username", u.getUsername())
                .addValue("password", u.getPassword())
                .addValue("email", u.getEmail())
                .addValue("firstName", u.getFirstName())
                .addValue("lastName", u.getLastName())
                .addValue("gender", u.getGender())
                .addValue("birthDate", u.getBirthDate())
                .addValue("phone", u.getPhone());

        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(sql, p, kh, new String[]{"id"});
        if (kh.getKey() != null) {
            u.setId(kh.getKey().longValue());
        }
        return u;
    }

    @Override
    public UserDto update(Long id, UserDto u) {
        String sql = "UPDATE users SET username=:username,password=:password,email=:email," +
                "first_name=:firstName,last_name=:lastName,gender=:gender,birth_date=:birthDate,phone=:phone " +
                "WHERE id=:id";

        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("username", u.getUsername())
                .addValue("password", u.getPassword())
                .addValue("email", u.getEmail())
                .addValue("firstName", u.getFirstName())
                .addValue("lastName", u.getLastName())
                .addValue("gender", u.getGender())
                .addValue("birthDate", u.getBirthDate())
                .addValue("phone", u.getPhone());

        jdbc.update(sql, p);
        u.setId(id);
        return u;
    }

    @Override
    public void delete(Long id) {
        jdbc.update("DELETE FROM users WHERE id=:id",
                new MapSqlParameterSource("id", id));
    }
}
