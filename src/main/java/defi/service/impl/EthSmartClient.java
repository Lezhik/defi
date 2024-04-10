package defi.service.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.http.HttpService;

import java.math.BigInteger;

@RequiredArgsConstructor
@Getter
@Setter
@Slf4j
public class EthSmartClient {
    final String node;
    Web3j client;
    int delay;

    public boolean init() {
        try {
            log.info("Start client to node {}", node);
            client = Web3j.build(new HttpService(node));
            return true;
        } catch (Throwable t) {
            log.error("Error on node {} init, message: {}", node, t.getMessage());
        }
        return false;
    }

    public boolean pingNode(long blockNum) {
        try {
            log.info("Ping node {}", node);
            var param = DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNum));
            var block = client.ethGetBlockByNumber(param, false).send().getBlock();
            if (block != null && block.getNumber().longValue() == blockNum) {
                log.info("Ping node {} successful", node);
                return true;
            }
            log.error("Ping node {} invalid", node);
        } catch (Throwable t) {
            log.error("Ping node {} invalid, message: {}", node, t.getMessage());
        }
        return false;
    }

    public void shutdown() {
        try {
            if (client != null) {
                client.shutdown();
                client = null;
            }
        } catch (Throwable t) {
            log.error("Error on node {} shutdown, message {}", node, t.getMessage());
        }
    }

    public boolean isActive() {
        return delay == 0;
    }

    public void decreasePriority() {
        delay++;
    }

    public void increasePriority() {
        if (delay > 0) {
            delay--;
        }
    }
}
