/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.swarmic.jta.narayana.weld;

import com.arjuna.ats.jta.TransactionManager;
import org.jboss.weld.transaction.spi.TransactionServices;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 *
 * This class is a simple implementation of Weld TransactionServices
 * using Narayana engine.
 *
 * It will be used by Weld to deal with transactional Observers
 *
 * @author Antoine Sabot-Durand
 */
public class NarayanaTransactionServices implements TransactionServices {


    /**
     * Registers a synchronization object with the currently executing transaction.
     *
     * @see javax.transaction.Synchronization
     * @param synchronizedObserver the synchronization
     */
    @Override
    public void registerSynchronization(Synchronization synchronizedObserver) {
        try {
            getTransactionManager().getTransaction().registerSynchronization(synchronizedObserver);
        } catch (IllegalStateException | RollbackException | SystemException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Queries the status of the current execution to see if a transaction is currently active.
     *
     * @return true if a transaction is active
     */
    @Override
    public boolean isTransactionActive() {
        try {
            final int status = getTransactionManager().getStatus();
            return status == Status.STATUS_ACTIVE ||
                    status == Status.STATUS_COMMITTING ||
                    status == Status.STATUS_MARKED_ROLLBACK ||
                    status == Status.STATUS_PREPARED ||
                    status == Status.STATUS_PREPARING ||
                    status == Status.STATUS_ROLLING_BACK;
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Obtain a reference to the JTA UserTransaction
     *
     * @return a reference to the JTA UserTransaction
     */
    @Override
    public UserTransaction getUserTransaction() {
        return com.arjuna.ats.jta.UserTransaction.userTransaction();
    }

    private javax.transaction.TransactionManager getTransactionManager() {
        return TransactionManager.transactionManager();
    }

    @Override
    public void cleanup() {

    }
}
