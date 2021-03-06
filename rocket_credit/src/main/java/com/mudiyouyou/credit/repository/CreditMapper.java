package com.mudiyouyou.credit.repository;

import com.mudiyouyou.credit.entity.Credit;
import com.mudiyouyou.credit.entity.CreditExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CreditMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table rocket_credit
     *
     * @mbg.generated
     */
    long countByExample(CreditExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table rocket_credit
     *
     * @mbg.generated
     */
    int insert(Credit record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table rocket_credit
     *
     * @mbg.generated
     */
    int insertSelective(Credit record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table rocket_credit
     *
     * @mbg.generated
     */
    List<Credit> selectByExample(CreditExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table rocket_credit
     *
     * @mbg.generated
     */
    int updateByExampleSelective(@Param("record") Credit record, @Param("example") CreditExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table rocket_credit
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") Credit record, @Param("example") CreditExample example);
}