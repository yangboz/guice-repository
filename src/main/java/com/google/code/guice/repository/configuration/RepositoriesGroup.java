/*
 * Copyright (C) 2012 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.code.guice.repository.configuration;

import com.google.common.base.Predicate;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Pattern;

/**
 * Represents set of Repository search criterias bound to persistence unit.
 * Should be created via {@link RepositoriesGroupBuilder}.
 * Used tighly in {@link ScanningJpaRepositoryModule}.
 *
 * @author Alexey Krylov
 * @since 07.12.12
 */
public class RepositoriesGroup {

	/*===========================================[ INSTANCE VARIABLES ]===========*/

    private String persistenceUnitName;
    private Collection<String> repositoriesPackages;
    private Pattern inclusionPattern;
    private Pattern exclusionPattern;
    private Predicate<Class> inclusionPredicate;
    private Predicate<Class> exclusionPredicate;

	/*===========================================[ CONSTRUCTORS ]=================*/

    protected RepositoriesGroup(String repositoriesPackage, String persistenceUnitName) {
        this(Arrays.asList(repositoriesPackage), persistenceUnitName);
    }

    protected RepositoriesGroup(Collection<String> repositoriesPackages, String persistenceUnitName) {
        this.repositoriesPackages = new ArrayList<String>(repositoriesPackages);
        this.persistenceUnitName = persistenceUnitName;
    }

	/*===========================================[ CLASS METHODS ]================*/

    public Collection<String> getRepositoriesPackages() {
        return Collections.unmodifiableCollection(repositoriesPackages);
    }

    protected void setIncusionPattern(String inclusionPattern) {
        if (inclusionPattern != null && !inclusionPattern.isEmpty()) {
            this.inclusionPattern = Pattern.compile(inclusionPattern);
            if (inclusionPredicate == null) {
                // inclusionPattern is a strict parameter - we need to deny other matcher possibilities
                inclusionPredicate = RepositoriesGroupFilterPredicates.DenyAll;
            }
        }
    }

    protected void setExclusionPattern(String exclusionPattern) {
        if (exclusionPattern != null && !exclusionPattern.isEmpty()) {
            this.exclusionPattern = Pattern.compile(exclusionPattern);
        }
    }

    protected void setInclusionFilterPredicate(Predicate<Class> inclusionPredicate) {
        if (inclusionPredicate != null) {
            this.inclusionPredicate = inclusionPredicate;
            if (inclusionPattern == null) {
                // strict parameters set - we need to deny other matcher possibilities
                inclusionPattern = Pattern.compile(RepositoriesGroupPatterns.DenyAll);
            }
        }
    }

    protected void setExclusionFilterPredicate(Predicate<Class> exclusionPredicate) {
        if (exclusionPredicate != null) {
            this.exclusionPredicate = exclusionPredicate;
        }
    }

    public Predicate<Class> getInclusionFilterPredicate() {
        return inclusionPredicate;
    }

    public Predicate<Class> getExclusionFilterPredicate() {
        return exclusionPredicate;
    }

    public boolean matches(Class<?> repositoryClass) {
        Assert.notNull(repositoryClass);
        String className = repositoryClass.getName();

        boolean matches = false;
        if (isInclusionPatternMatches(className) || isInclusionFilterPredicateMatches(repositoryClass)) {
            matches = true;
        }

        if (matches) {
            if (isExclusionPatternMatches(className) || isXxclusionFilterPredicateMatches(repositoryClass)) {
                matches = false;
            }
        }

        return matches;
    }

    private boolean isInclusionPatternMatches(CharSequence className) {
        return inclusionPattern == null || inclusionPattern.matcher(className).matches();
    }

    private boolean isInclusionFilterPredicateMatches(Class<?> repositoryClass) {
        return inclusionPredicate == null || inclusionPredicate.apply(repositoryClass);
    }

    private boolean isExclusionPatternMatches(CharSequence className) {
        return exclusionPattern != null && exclusionPattern.matcher(className).matches();
    }

    private boolean isXxclusionFilterPredicateMatches(Class<?> repositoryClass) {
        return exclusionPredicate != null && exclusionPredicate.apply(repositoryClass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RepositoriesGroup)) {
            return false;
        }

        RepositoriesGroup repositoriesGroup = (RepositoriesGroup) o;

        if (exclusionPattern != null ? !exclusionPattern.equals(repositoriesGroup.exclusionPattern) : repositoriesGroup.exclusionPattern != null) {
            return false;
        }
        if (exclusionPredicate != null ? !exclusionPredicate.equals(repositoriesGroup.exclusionPredicate) : repositoriesGroup.exclusionPredicate != null) {
            return false;
        }
        if (inclusionPattern != null ? !inclusionPattern.equals(repositoriesGroup.inclusionPattern) : repositoriesGroup.inclusionPattern != null) {
            return false;
        }
        if (inclusionPredicate != null ? !inclusionPredicate.equals(repositoriesGroup.inclusionPredicate) : repositoriesGroup.inclusionPredicate != null) {
            return false;
        }
        if (persistenceUnitName != null ? !persistenceUnitName.equals(repositoriesGroup.persistenceUnitName) : repositoriesGroup.persistenceUnitName != null) {
            return false;
        }
        if (repositoriesPackages != null ? !repositoriesPackages.equals(repositoriesGroup.repositoriesPackages) : repositoriesGroup.repositoriesPackages != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = persistenceUnitName != null ? persistenceUnitName.hashCode() : 0;
        result = 31 * result + (repositoriesPackages != null ? repositoriesPackages.hashCode() : 0);
        result = 31 * result + (inclusionPattern != null ? inclusionPattern.hashCode() : 0);
        result = 31 * result + (exclusionPattern != null ? exclusionPattern.hashCode() : 0);
        result = 31 * result + (inclusionPredicate != null ? inclusionPredicate.hashCode() : 0);
        result = 31 * result + (exclusionPredicate != null ? exclusionPredicate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RepositoriesGroup");
        sb.append("{persistenceUnitName='").append(persistenceUnitName).append('\'');
        sb.append(", repositoriesPackages=").append(repositoriesPackages);
        sb.append(", inclusionPattern=").append(inclusionPattern);
        sb.append(", exclusionPattern=").append(exclusionPattern);
        sb.append('}');
        return sb.toString();
    }

	/*===========================================[ GETTER/SETTER ]================*/

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public Pattern getInclusionPattern() {
        return inclusionPattern;
    }

    public Pattern getExclusionPattern() {
        return exclusionPattern;
    }
}