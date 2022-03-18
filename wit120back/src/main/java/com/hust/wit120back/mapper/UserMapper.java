package com.hust.wit120back.mapper;

import com.hust.wit120back.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {
    @Insert("insert into user(username, password, phone, permission) values(#{username}, #{password}, #{phone}, #{permission})")
    void addUser(User user);

    @Select("select * from user where username=#{username} and password=#{password}")
    @Result(column = "user_id", property = "userId")
    User selectUserByUsernameAndPassword(String username, String password);

    @Select("select * from user where user_id=#{userId}")
    @Result(column = "user_id", property = "userId")
    User selectUserByUserId(Integer userId);

    @Select("select * from user where phone=#{phone}")
    @Result(column = "user_id", property = "userId")
    User selectUserByPhone(String phoneNum);

    @Select("select * from user where username=#{username}")
    @Result(column = "user_id", property = "userId")
    User selectUserByUsername(String username);

    @Update("update user set password=#{password} where username=#{username}")
    int updatePassword(String password, String username);

    @Select("select * from user where permission=#{permission} limit #{pageNum},#{pageSize}")
    @Result(column = "user_id", property = "userId")
    List<User> selectUserByPermission(int permission, int pageNum, int pageSize);

    @Select("select count(*) from user where permission=#{permission}")
    int selectTotalByPermission(int permission);

    @Select("select * from user where user_id=#{userId} and permission=#{permission}")
    @Result(column = "user_id", property = "userId")
    User selectUserByUserIdAndPermission(Integer userId, int permission);

    @Delete("delete from user where user_id=#{userId}")
    int deleteUserByUserId(Integer userId);
}
