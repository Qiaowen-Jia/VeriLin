package ticketingsystem;

import java.util.concurrent.atomic.AtomicReference;

public class OperationLock {
    private AtomicReference<Operation> operation;

    void lock(int expectedOperation) {
        while (true) {
            Operation oldOperation = operation.get();
            if (oldOperation.operation == expectedOperation || oldOperation.num == 0) {

            }
        }
    }

    static class Operation {
        int operation;
        int num;

        Operation(int operation, int num) {
            this.operation = operation;
            this.num = num;
        }

        Operation copy() {
            return new Operation(this.operation, this.num);
        }
    }
}
