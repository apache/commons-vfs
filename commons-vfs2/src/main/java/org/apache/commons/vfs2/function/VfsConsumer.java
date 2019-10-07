/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.vfs2.function;

import java.util.Objects;

import org.apache.commons.vfs2.FileSystemException;

/**
 * A {@link java.util.function.Consumer} that throws {@link FileSystemException}.
 *
 * @param <T> the type of the input to the operation
 *
 * @since 2.5.0
 */
@FunctionalInterface
public interface VfsConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws FileSystemException Thrown when VFS detects an error.
     */
    void accept(T t) throws FileSystemException;

    /**
     * Returns a composed {@code Consumer} that performs, in sequence, this operation followed by the {@code after}
     * operation. If performing either operation throws an exception, it is relayed to the caller of the composed
     * operation. If performing this operation throws an exception, the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code Consumer} that performs in sequence this operation followed by the {@code after}
     *         operation
     * @throws NullPointerException if {@code after} is null
     */
    default VfsConsumer<T> andThen(VfsConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }
}