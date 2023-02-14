/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.component;

import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.component.SoftwareComponentContainer;
import org.gradle.api.internal.CollectionCallbackActionDecorator;
import org.gradle.api.internal.DefaultPolymorphicDomainObjectContainer;
import org.gradle.api.provider.Property;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.internal.reflect.Instantiator;

import javax.inject.Inject;

/**
 * Default implementation of {@link SoftwareComponentContainer}.
 */
public abstract class DefaultSoftwareComponentContainer extends DefaultPolymorphicDomainObjectContainer<SoftwareComponent> implements SoftwareComponentContainerInternal, HasPublicType {

    @Inject
    public DefaultSoftwareComponentContainer(Instantiator instantiator, CollectionCallbackActionDecorator decorator) {
        super(SoftwareComponent.class, instantiator, instantiator, decorator);
    }

    @Override
    public abstract Property<SoftwareComponent> getMainComponent();

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(SoftwareComponentContainer.class);
    }
}
