package com.ThinkJob.thinkjob.User;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class UserDAO {
	private final JdbcTemplate jdbcTemplate;
}