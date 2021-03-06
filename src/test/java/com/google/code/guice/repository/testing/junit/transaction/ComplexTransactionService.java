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

package com.google.code.guice.repository.testing.junit.transaction;

import com.google.code.guice.repository.testing.model.Account;
import com.google.code.guice.repository.testing.model.User;
import com.google.code.guice.repository.testing.repo.AccountRepository;
import com.google.code.guice.repository.testing.repo.UserRepository;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("MagicNumber")
public class ComplexTransactionService {

    /*===========================================[ STATIC VARIABLES ]=============*/

    private static final Logger logger = LoggerFactory.getLogger(ComplexTransactionService.class);

    /*===========================================[ INSTANCE VARIABLES ]===========*/

    @Inject
    private UserRepository userRepository;

    @Inject
    private AccountRepository accountRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /*===========================================[ CLASS METHODS ]================*/

    @Transactional(rollbackFor = Exception.class)
    public void performFirstComplexTransaction() throws Exception {
        logger.info("EM for performComplexTransaction: " + entityManager);
        logger.info("DeleteAll");
        userRepository.deleteAll();
        logger.info("Save1");
        userRepository.save(new User("John", "Smith", 42));
        logger.info("Save2");
        userRepository.save(new User("Alex", "Johns", 22));
        logger.info("Count1");
        accountRepository.save(new Account(UUID.randomUUID().toString(), "name"));
        // partially committed
        Assert.assertEquals("Invalid repository size", 2, userRepository.count());
        Assert.assertEquals("Invalid repository size", 1, accountRepository.count());
        throw new Exception("First rollback");
    }

    @Transactional(rollbackFor = Exception.class)
    public void performSecondComplexTransaction() throws Exception {
        logger.info("Checking size");
        Assert.assertEquals("Invalid repository size", 0, userRepository.count());
        logger.info("EM for performSecondComplexTransaction: " + entityManager);
        logger.info("Save3");
        userRepository.save(new User("1", "1", 1));
        logger.info("Count2");
        Assert.assertEquals("Invalid repository size", 1, userRepository.count());
        throw new Exception("Second rollback");
    }

    @Transactional(rollbackFor = Exception.class)
    public void performThirdComplexTransaction() throws Exception {
        userRepository.deleteAll();
        accountRepository.deleteAll();
        throw new Exception("Third rollback");
    }

    @Transactional(timeout = 1, rollbackFor = Exception.class)
    public void testTimeoutedTransaction() throws Exception {
        TimeUnit.SECONDS.sleep(3);
        userRepository.findAll();
    }
}