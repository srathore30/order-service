package sfa.order_service.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sfa.order_service.entity.OrderEntity;
import sfa.order_service.entity.OrderInvoice;

import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class TallyService {

//    @Value("${tally.url}")
//    private String tallyUrl;

    private static final String TALLY_URL = "http://localhost:9000";

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendRequestToTally(String xmlRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);

        HttpEntity<String> entityHttp = new HttpEntity<>(xmlRequest, headers);
        return restTemplate.postForObject(TALLY_URL, entityHttp, String.class);
    }

    // Bulk Order Invoice XML
    public String buildOrderVoucherXml(OrderInvoice invoice, List<OrderEntity> orders) {
        StringBuilder xml = new StringBuilder();
        xml.append("<ENVELOPE>")
                .append("<HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER>")
                .append("<BODY><IMPORTDATA>")
                .append("<REQUESTDESC><REPORTNAME>Vouchers</REPORTNAME></REQUESTDESC>")
                .append("<REQUESTDATA><TALLYMESSAGE xmlns:UDF=\"TallyUDF\">")
                .append("<VOUCHER VCHTYPE=\"Sales\" ACTION=\"Create\">");

        // Invoice header
        xml.append("<DATE>")
                .append(new SimpleDateFormat("yyyyMMdd").format(invoice.getInvoiceDate()))
                .append("</DATE>");
        xml.append("<VOUCHERNUMBER>").append(invoice.getInvoiceNumber()).append("</VOUCHERNUMBER>");
        xml.append("<PARTYNAME>").append("Customer-" + invoice.getMemberId()).append("</PARTYNAME>");

        // Each order line
        for (OrderEntity order : orders) {
            double lineAmount = (order.getPriceAfterDiscount() != null)
                    ? order.getPriceAfterDiscount()
                    : order.getPrice() * order.getQuantity();

            xml.append("<ALLLEDGERENTRIES.LIST>")
                    .append("<LEDGERNAME>").append("Product-" + order.getProductId()).append("</LEDGERNAME>")
                    .append("<ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>")
                    .append("<AMOUNT>").append(lineAmount).append("</AMOUNT>")
                    .append("</ALLLEDGERENTRIES.LIST>");
        }

        xml.append("</VOUCHER></TALLYMESSAGE></REQUESTDATA></IMPORTDATA></BODY></ENVELOPE>");
        return xml.toString();
    }


}
