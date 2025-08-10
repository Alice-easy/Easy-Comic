package com.easycomic.domain.usecase

/**
 * 用例基类接口
 */
interface BaseUseCase<in P, out R> {
    suspend operator fun invoke(parameters: P): R
}

/**
 * 无参数用例基类
 */
interface NoParametersUseCase<out R> {
    suspend operator fun invoke(): R
}