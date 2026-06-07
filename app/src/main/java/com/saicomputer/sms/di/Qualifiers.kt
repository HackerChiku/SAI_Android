package com.saicomputer.sms.di

import javax.inject.Qualifier

/** The deployed Apps Script /exec URL, injected from BuildConfig. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WebAppUrl
