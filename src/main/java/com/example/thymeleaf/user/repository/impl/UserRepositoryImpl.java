package com.example.thymeleaf.user.repository.impl;

import com.example.thymeleaf.common.PageData;
import com.example.thymeleaf.user.dto.UserDto;
import com.example.thymeleaf.user.repository.UserRepository;
import java.time.LocalDate;
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

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM users ");
        sql.append(where).append(" ");
        sql.append("ORDER BY ").append(orderBy).append(" ").append(direction).append(" ");
        sql.append("LIMIT :size OFFSET :offset");

        List<UserDto> content = jdbc.query(sql.toString(), params, MAPPER);
        return new PageData<>(content, page, size, total);
    }

    @Override
    public UserDto findById(Long id) {
        return jdbc.queryForObject("SELECT * FROM users WHERE id=:id",
                new MapSqlParameterSource("id", id), MAPPER);
    }

    @Override
    public UserDto insert(UserDto u) {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO users (");
        sql.append("username, ");
        sql.append("password, ");
        sql.append("email, ");
        sql.append("first_name, ");
        sql.append("last_name, ");
        sql.append("gender, ");
        sql.append("birth_date, ");
        sql.append("phone");
        sql.append(") VALUES (");
        sql.append(":username, ");
        sql.append(":password, ");
        sql.append(":email, ");
        sql.append(":firstName, ");
        sql.append(":lastName, ");
        sql.append(":gender, ");
        sql.append(":birthDate, ");
        sql.append(":phone");
        sql.append(")");

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
        jdbc.update(sql.toString(), p, kh, new String[]{"id"});
        if (kh.getKey() != null) {
            u.setId(kh.getKey().longValue());
        }
        return u;
    }

    @Override
    public UserDto update(Long id, UserDto u) {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE users SET ");
        sql.append("username=:username, ");
        sql.append("password=:password, ");
        sql.append("email=:email, ");
        sql.append("first_name=:firstName, ");
        sql.append("last_name=:lastName, ");
        sql.append("gender=:gender, ");
        sql.append("birth_date=:birthDate, ");
        sql.append("phone=:phone ");
        sql.append("WHERE id=:id");

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

        jdbc.update(sql.toString(), p);
        u.setId(id);
        return u;
    }

    @Override
    public void delete(Long id) {
        jdbc.update("DELETE FROM users WHERE id=:id",
                new MapSqlParameterSource("id", id));
    }
}
