package com.example.nojoto.pojo

    data class RetorfiterrorBody(
        val className: String,
        val code: Int,
        val errors: Errors,
        val message: String,
        val name: String
    )

    data class Errors(
        val e: String
    )
