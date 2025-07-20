package com.tickeTeam.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE}) // 적용 대상
@Retention(RetentionPolicy.RUNTIME) // 생명 주기
public @interface Trace {
}
