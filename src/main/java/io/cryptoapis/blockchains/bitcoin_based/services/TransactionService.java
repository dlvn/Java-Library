package io.cryptoapis.blockchains.bitcoin_based.services;

import io.cryptoapis.abstractServices.AbstractServicesConfig;
import io.cryptoapis.blockchains.bitcoin_based.models.Hex;
import io.cryptoapis.blockchains.bitcoin_based.models.Transaction.CompleteTransaction;
import io.cryptoapis.blockchains.bitcoin_based.models.Transaction.CreateHDWalletTransaction;
import io.cryptoapis.blockchains.bitcoin_based.models.Transaction.CreateTransaction;
import io.cryptoapis.blockchains.bitcoin_based.models.Transaction.RefundTransaction;
import io.cryptoapis.blockchains.bitcoin_based.models.Transaction.SignTransaction;
import io.cryptoapis.blockchains.bitcoin_based.models.Transaction.TransactionSize;
import io.cryptoapis.common_models.ApiError;
import io.cryptoapis.common_models.ApiResponse;
import io.cryptoapis.utils.Utils;
import io.cryptoapis.utils.config.EndpointConfig;
import io.cryptoapis.utils.enums.HttpsRequestsEnum;
import io.cryptoapis.utils.rest.WebServices;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class TransactionService extends AbstractServicesConfig {
    private static final String PATH = "/{0}/bc/{1}/{2}/txs/{3}";

    public TransactionService(EndpointConfig endpointConfig) {
        super(endpointConfig);
    }

    @Override
    protected String getPath() {
        return PATH;
    }

    public ApiResponse getTxByHash(String hash) {
        String endpoint = String.format("txid/%s", hash);
        return getTransaction(endpoint, null);
    }

    public ApiResponse getTxByBlock(String blockHash, Map<String, String> params) {
        String endpoint = String.format("block/%s", blockHash);

        Entry<String, ApiError> pair = Utils.setQueryParams(params);
        if (pair.getValue() != null) {
            return Utils.setApiResponse(pair.getValue());
        }
        return getTransaction(endpoint, pair.getKey());
    }

    public ApiResponse getTxByBlock(int blockNumber, Map<String, String> params) {
        String endpoint = String.format("block/%s", blockNumber);

        Entry<String, ApiError> pair = Utils.setQueryParams(params);
        if (pair.getValue() != null) {
            return Utils.setApiResponse(pair.getValue());
        }
        return getTransaction(endpoint, pair.getKey());
    }

    public ApiResponse getUnconfirmedTxs(Map<String, String> params) {
        Entry<String, ApiError> pair = Utils.setQueryParams(params);
        if (pair.getValue() != null) {
            return Utils.setApiResponse(pair.getValue());
        }

        return WebServices.httpsRequest(WebServices.formatUrl(url.concat(pair.getKey()), endpointConfig, "unconfirmed"), HttpsRequestsEnum.GET.name(),
                endpointConfig, null);
    }

    public ApiResponse decodeTx(String hex) {
        return WebServices.httpsRequest(WebServices.formatUrl(url, endpointConfig, "decode"), HttpsRequestsEnum.POST.name(), endpointConfig,
                Hex.createHex(hex).toString());
    }

    //only creates the transaction
    public ApiResponse createTx(List<CreateTransaction.Inputs> inputs, List<CreateTransaction.Outputs> outputs, CreateTransaction.Fee fee, Integer locktime) {
        CreateTransaction createTransaction = CreateTransaction.createTx(inputs, outputs, fee, locktime);

        return WebServices.httpsRequest(WebServices.formatUrl(url, endpointConfig, "create"), HttpsRequestsEnum.POST.name(),
                endpointConfig, createTransaction.toString());
    }

    public ApiResponse signTx(String hex, List<String> wifs) {
        return WebServices.httpsRequest(WebServices.formatUrl(url, endpointConfig, "sign"), HttpsRequestsEnum.POST.name(), endpointConfig,
                SignTransaction.sign(hex, wifs).toString());
    }

    public ApiResponse sendTx(String hex) {
        return WebServices.httpsRequest(WebServices.formatUrl(url, endpointConfig, "send"), HttpsRequestsEnum.POST.name(), endpointConfig,
                Hex.createHex(hex).toString());
    }

    //create, sign and send tx to blockchain
    public ApiResponse newTx(CreateTransaction createTx, List<String> wifs) {
        return WebServices.httpsRequest(WebServices.formatUrl(url, endpointConfig, "new"), HttpsRequestsEnum.POST.name(), endpointConfig,
                CompleteTransaction.createSignAndSend(createTx, wifs).toString());
    }

    public ApiResponse newTxWithHDWallet(String walletName, String password, List<CreateTransaction.Inputs> inputs,
                                         List<CreateTransaction.Outputs> outputs, CreateTransaction.Fee fee, Integer locktime) {
        return WebServices.httpsRequest(WebServices.formatUrl(url, endpointConfig, "hdwallet"), HttpsRequestsEnum.POST.name(), endpointConfig,
                CreateHDWalletTransaction.create(walletName, password, inputs, outputs, fee, locktime).toString());
    }

    public ApiResponse transactionSizeWithFee(List<TransactionSize.Inputs> inputs, List<TransactionSize.Outputs> outputs, TransactionSize.Fee fee, Integer locktime) {
        TransactionSize transactionSize = TransactionSize.transactionSizeWithFee(inputs, outputs, fee, locktime);

        return WebServices.httpsRequest(WebServices.formatUrl(url, endpointConfig, "size"), HttpsRequestsEnum.POST.name(), endpointConfig,
                transactionSize.toString());
    }

    public ApiResponse transactionSize(List<TransactionSize.Inputs> inputs, List<TransactionSize.Outputs> outputs, Integer locktime) {
        TransactionSize transactionSize = TransactionSize.transactionSize(inputs, outputs, locktime);

        return WebServices.httpsRequest(WebServices.formatUrl(url, endpointConfig, "size"), HttpsRequestsEnum.POST.name(), endpointConfig,
                transactionSize.toString());
    }

    public ApiResponse getFees() {
        return getTransaction("fee", null);
    }

    public ApiResponse refundTx(String txid, String wif, String fee) {
        String endpoint = "refund";

        return WebServices.httpsRequest(WebServices.formatUrl(url, endpointConfig, endpoint), HttpsRequestsEnum.POST.name(), endpointConfig,
                RefundTransaction.refundTx(txid, wif, fee).toString());
    }

    private ApiResponse getTransaction(String endpoint, String params) {
        if (params != null) {
            url = url.concat(params);
        }

        return WebServices.httpsRequest(WebServices.formatUrl(url, endpointConfig, endpoint), HttpsRequestsEnum.GET.name(), endpointConfig, null);
    }

}
