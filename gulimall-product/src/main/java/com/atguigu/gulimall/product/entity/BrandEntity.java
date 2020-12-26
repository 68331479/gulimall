package com.atguigu.gulimall.product.entity;

import com.atguigu.common.valid.AddGroup;
import com.atguigu.common.valid.ListValue;
import com.atguigu.common.valid.UpdateGroup;
import com.atguigu.common.valid.UpdateStatusGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 品牌
 * 
 * @author ldeng
 * @email 68331479@qq.com
 * @date 2020-11-11 17:14:39
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */

	@NotNull(message = "修改必须指定品牌id",groups = {UpdateGroup.class})
	@Null(message="新增不能指定id",groups = {AddGroup.class})
	@TableId
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotEmpty(message ="品牌名不能为空",groups = {AddGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */
	@NotEmpty(message = "logo不能为空",groups = {AddGroup.class})
	@URL(message ="logo 必须是合法的url",groups = {AddGroup.class,UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */
	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(groups = {AddGroup.class,UpdateStatusGroup.class})
	@ListValue(vals={0,1},groups = {AddGroup.class,UpdateStatusGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */

	@NotEmpty(message = "首字母不能为空",groups = {AddGroup.class})
	@Pattern(regexp="^[a-zA-Z]$",message = "自定义校验规则-->检索字母必须是一个字母",groups = {AddGroup.class,UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(groups = {AddGroup.class})
	@Min(value=0,message ="排序必须大于等于0",groups = {AddGroup.class,UpdateGroup.class})
	private Integer sort;

}
