package com.mudiyouyou.credit.entity;

public class Credit {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column rocket_credit.id
     *
     * @mbg.generated
     */
    private Integer id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column rocket_credit.user_id
     *
     * @mbg.generated
     */
    private String userId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column rocket_credit.credit
     *
     * @mbg.generated
     */
    private Integer credit;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column rocket_credit.freeze_credit
     *
     * @mbg.generated
     */
    private Integer freezeCredit;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column rocket_credit.id
     *
     * @return the value of rocket_credit.id
     *
     * @mbg.generated
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column rocket_credit.id
     *
     * @param id the value for rocket_credit.id
     *
     * @mbg.generated
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column rocket_credit.user_id
     *
     * @return the value of rocket_credit.user_id
     *
     * @mbg.generated
     */
    public String getUserId() {
        return userId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column rocket_credit.user_id
     *
     * @param userId the value for rocket_credit.user_id
     *
     * @mbg.generated
     */
    public void setUserId(String userId) {
        this.userId = userId == null ? null : userId.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column rocket_credit.credit
     *
     * @return the value of rocket_credit.credit
     *
     * @mbg.generated
     */
    public Integer getCredit() {
        return credit;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column rocket_credit.credit
     *
     * @param credit the value for rocket_credit.credit
     *
     * @mbg.generated
     */
    public void setCredit(Integer credit) {
        this.credit = credit;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column rocket_credit.freeze_credit
     *
     * @return the value of rocket_credit.freeze_credit
     *
     * @mbg.generated
     */
    public Integer getFreezeCredit() {
        return freezeCredit;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column rocket_credit.freeze_credit
     *
     * @param freezeCredit the value for rocket_credit.freeze_credit
     *
     * @mbg.generated
     */
    public void setFreezeCredit(Integer freezeCredit) {
        this.freezeCredit = freezeCredit;
    }
}