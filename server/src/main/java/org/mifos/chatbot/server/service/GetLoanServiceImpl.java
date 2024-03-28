package org.mifos.chatbot.server.service;

import lombok.extern.slf4j.Slf4j;
import org.mifos.chatbot.server.config.openFeign.Response.*;
import org.mifos.chatbot.server.model.ClientCountapi;
import org.mifos.chatbot.server.model.LoanAccount;
import org.mifos.chatbot.server.model.LoanAccounts;
import org.mifos.chatbot.server.model.Tracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
@Slf4j
@Service
public class GetLoanServiceImpl {

    @Autowired
    private FineractServiceImpl fineractService;

    Helper helper = new Helper();

    public String getLoanStatus(String botResponse, Tracker tracker, HttpServletRequest request) {
        int clientId = Integer.parseInt(this.getSlot("client_id_slot", tracker));
        LoanAccounts loanAccounts = fineractService.getClientDetails(clientId, request);
        List<LoanAccount> loanAccountList = loanAccounts.getLoanAccounts();
        String res;
        if (loanAccountList!=null){
            res = "You have "+ loanAccountList.size() + " Loan Account";
            for(LoanAccount account : loanAccountList) {
                String loanAccountStatus = account.getStatus().getValue();
                res = res.concat( " " + account.getAccountNo() + " (" +  account.getProductName() + ") " + " - " + loanAccountStatus) + " \n";
            }
        } else {
            res="This Client have no Loan Account";
        }
        return res;
    }

    public Integer getApprovedPrincipalAmount(String botResponse, HttpServletRequest request) {
        GetLoansResponse getLoansResponse = fineractService.getLoanDetails(botResponse, request);
        return getLoansResponse.getApprovedPrincipal();
    }
    public String getClientCount(String botResponse, HttpServletRequest request) {
        ClientCountapi NoofClients = fineractService.getAllClient(request);
        log.info(NoofClients.toString());
        return "The Number of Clients is/are " + NoofClients.getContent().size();
    }
    public Integer getInterestRate(String botResponse, HttpServletRequest request) {
        GetLoansResponse getLoansResponse = fineractService.getLoanDetails(botResponse, request);
        return getLoansResponse.getAnnualInterestRate();
    }

    public String getMaturityDate(String botResponse, HttpServletRequest request) {
        GetLoansResponse getLoansResponse = fineractService.getLoanDetails(botResponse , request);
        Timeline time = getLoansResponse.getTimeline();
        List<Integer> dateList = time.getExpectedMaturityDate();
        return helper.getDate(dateList);
    }

    public String getPreviousPaymentDate(String botResponse , HttpServletRequest request){
        GetLoansResponse getLoansResponse = fineractService.getLoanDetails(botResponse , request);
        List<Transaction> transactions = getLoansResponse.getTransactions();
        if(!transactions.isEmpty()) {
            Transaction latest = transactions.get(transactions.size()-1);
            return helper.getDate(latest.getDate());
        }
        return "No payments made";
    }

    public String getPreviousPaymentAmount (String botResponse , HttpServletRequest request) {
        GetLoansResponse getLoansResponse = fineractService.getLoanDetails(botResponse , request);
        List<Transaction> transactions = getLoansResponse.getTransactions();
        if(!transactions.isEmpty()) {
            Transaction latest = transactions.get(transactions.size()-1);
            return latest.getAmount().toString();
        }
        return "No payments made";
    }

    public String getPreviousPaymentInterest(String botResponse, HttpServletRequest request){
        GetLoansResponse getLoansResponse = fineractService.getLoanDetails(botResponse , request);
        List<Transaction> transactions = getLoansResponse.getTransactions();
        if(!transactions.isEmpty()) {
            Transaction latest = transactions.get(transactions.size()-1);
            return latest.getInterestPortion().toString();
        }
        return "No payments made";
    }

    public String getNextDueDate(String botResponse, HttpServletRequest request) {
        GetLoansResponse getLoansResponse = fineractService.getLoanDetails(botResponse , request);
        RepaymentSchedule schedule = getLoansResponse.getRepaymentSchedule();
        List<Period> periods = schedule.getPeriods();
        periods.remove(0);
        for(Period period : periods) {
            if(!period.isComplete()) {
                return helper.getDate(period.getDueDate());
            }
        }
        return "No due items";
    }

    public String getNextDuePrincipal(String botResponse , HttpServletRequest request){
        GetLoansResponse getLoansResponse = fineractService.getLoanDetails(botResponse , request);
        RepaymentSchedule schedule = getLoansResponse.getRepaymentSchedule();
        List<Period> periods = schedule.getPeriods();
        periods.remove(0);
        for(Period period : periods) {
            if(!period.isComplete()) {
                return String.valueOf(period.getTotalOutstandingForPeriod());
            }
        }
        return "No due items";
    }

    public String getArrearDays(String botResponse, HttpServletRequest request) {
        GetLoansResponse getLoansResponse = fineractService.getLoanDetails(botResponse , request);
        Delinquent delinquent = getLoansResponse.getDelinquent();
        if(delinquent.getDelinquentDate() != null) {
            return helper.getDate(delinquent.getDelinquentDate());
        }
        return "No arrears";
    }

    public String getLoanDisbursedDate(String botResponse, HttpServletRequest request) {
        GetLoansResponse getLoansResponse = fineractService.getLoanDetails(botResponse , request);
        Timeline time = getLoansResponse.getTimeline();
        List<Integer> dateList = time.getActualDisbursementDate();
        return helper.getDate(dateList);
    }

    public String getDisbursementAmount(String botResponse , HttpServletRequest request) {
        GetLoansResponse getLoansResponse = fineractService.getLoanDetails(botResponse , request);
        RepaymentSchedule schedule = getLoansResponse.getRepaymentSchedule();
        return String.valueOf(schedule.getTotalPrincipalDisbursed());
    }

    public String getLoanApprovedDate(String botResponse, HttpServletRequest request) {
        GetLoansResponse getLoansResponse = fineractService.getLoanDetails(botResponse , request);
        Timeline time = getLoansResponse.getTimeline();
        List<Integer> dateList = time.getApprovedOnDate();
        return helper.getDate(dateList);
    }

    public String getFirstRepaymentDate(String botResponse , HttpServletRequest request) {
        GetLoansResponse getLoansResponse = fineractService.getLoanDetails(botResponse , request);
        RepaymentSchedule schedule = getLoansResponse.getRepaymentSchedule();
        List<Period> periods = schedule.getPeriods();
        periods.remove(0);
        return helper.getDate(periods.get(0).getDueDate());
    }

    public String getClientActivationDate(String botResponse, HttpServletRequest request) {
        GetClientInfoResponse response = fineractService.getClientInfo(request);
        List<Integer> date = response.getActivationDate();
        return helper.getDate(date);
    }

    public void authorization(String text) {
    }

    private String getSlot(String slotName, Tracker tracker) {
        return tracker.getSlots().get(slotName);
    }
}

