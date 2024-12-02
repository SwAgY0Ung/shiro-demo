package com.yhy.shiro.shiro_springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * 
     */
    @TableId
    private Integer id;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String phone;

    /**
     * 账号
     */
    private String account;

    /**
     * 
     */
    private String pwd;

    /**
     * 盐
     */
    private String salt;

    /**
     * token
     */
    @TableField(exist = false)
    private String token;

    @TableField(exist = false)
    private List<String> roles;

    @TableField(exist = false)
    private List<String> funcs;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}