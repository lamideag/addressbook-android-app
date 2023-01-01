package com.deepschneider.addressbook.utils

object Constants {
    const val NO_VALUE = "no value"
    const val TOKEN_KEY = "token"

    const val AUTH_FAILURE_MESSAGE = "WRONG LOGIN OR PASSWORD"
    const val FORBIDDEN_MESSAGE = "WRONG LOGIN OR PASSWORD"
    const val SERVER_TIMEOUT_MESSAGE = "SERVER CONNECTION TIMEOUT"

    const val ORGANIZATIONS_CACHE_NAME = "com.addressbook.model.Organization"
    const val PERSONS_CACHE_NAME = "com.addressbook.model.Person"

    const val ORGANIZATIONS_ID_FIELD = "id"
    const val ORGANIZATIONS_NAME_FIELD = "name"
    const val ORGANIZATIONS_ADDRESS_FIELD = "street"
    const val ORGANIZATIONS_ZIP_FIELD = "zip"
    const val ORGANIZATIONS_TYPE_FIELD = "type"
    const val ORGANIZATIONS_LAST_UPDATED_FIELD = "lastUpdated"

    const val PERSONS_ID_FIELD = "id"
    const val PERSONS_ORG_ID__FIELD = "orgId"
    const val PERSONS_FIRST_NAME_FIELD = "firstName"
    const val PERSONS_LAST_NAME_FIELD = "lastName"
    const val PERSONS_RESUME_FIELD = "resume"
    const val PERSONS_SALARY_FIELD = "salary"

    const val SORT_ORDER_DESC = "desc"
    const val SORT_ORDER_ASC = "asc"
}