package com.kotlinkhaos.classes.errors;

abstract class UserError(message: String) : Exception(message)

class UserNetworkError : UserError("A network error has occurred.")

class UserCreateStreamError : UserError("Unable to open image stream")

class UserUploadS3Error(message: String) : UserError(message)

class UserApiError(status: Int, message: String) : UserError(message)