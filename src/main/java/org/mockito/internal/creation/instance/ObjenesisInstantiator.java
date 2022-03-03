/*
 * Copyright (c) 2016 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.creation.instance;

import net.bytebuddy.utility.GraalImageCode;
import org.mockito.creation.instance.Instantiator;
import org.mockito.internal.configuration.GlobalConfiguration;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisBase;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.instantiator.sun.UnsafeFactoryInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;

class ObjenesisInstantiator implements Instantiator {

    // TODO: in order to provide decent exception message when objenesis is not found,
    // have a constructor in this class that tries to instantiate ObjenesisStd and if it fails then
    // show decent exception that dependency is missing
    // TODO: for the same reason catch and give better feedback when hamcrest core is not found.
    private final Objenesis objenesis = GraalImageCode.getCurrent().isDefined()
        ? new ObjenesisBase(new UnsafeInstantiatorStrategy(), true)
        : new ObjenesisStd(new GlobalConfiguration().enableClassCache());

    @Override
    public <T> T newInstance(Class<T> cls) {
        return objenesis.newInstance(cls);
    }

    private static class UnsafeInstantiatorStrategy implements InstantiatorStrategy {

        @Override
        @SuppressWarnings("CheckReturnValue")
        public <T> ObjectInstantiator<T> newInstantiatorOf(Class<T> type) {
            type.getDeclaredConstructors(); // Graal does not track Unsafe constructors.
            return new UnsafeFactoryInstantiator<>(type);
        }
    }
}
