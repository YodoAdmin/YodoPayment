package co.yodo.mobile.business.injection.scope;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

@Retention( RetentionPolicy.RUNTIME )
@Scope
public @interface ApplicationScope {
}
