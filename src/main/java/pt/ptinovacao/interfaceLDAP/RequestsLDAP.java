package pt.ptinovacao.interfaceLDAP;

import pt.ptinovacao.InterfaceHTTP.Configurations;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.math.BigDecimal;
import java.util.Hashtable;
import java.util.logging.Logger;

public class RequestsLDAP implements Requests {

    private final Logger logger;

    String urlCreateClientLdap = Configurations.getUrlLDAP();


    String ldapUrl = urlCreateClientLdap; //urlCreateClientLdap;

    String username = "DC=C-NTDB";

    String password = "secret";

    DirContext ctx = null;

    public RequestsLDAP(Logger logger) {
        this.logger = logger;

        // Set up the environment for creating the initial context
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, ldapUrl);
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);

        try {
            // Create the initial context
            ctx = new InitialDirContext(env);
            System.out.println("Connected to LDAP server successfully!");
        } catch (NamingException e) {
            e.printStackTrace();
            System.out.println("Failed to connect to LDAP server.");
        }
    }

    public void closeLDAP() {
        logger.info("[RequestLdap]: Closing LDAP connection");

        if (ctx != null) {
            try {
                ctx.close();
            } catch (NamingException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean creditBucket(String account, String amount) {

        try {
            String serviceDn = "pcsServiceId=200,subdata=pcsProf,subdata=Profile,ds=pcs,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";
            String bytes = Configurations.convertMoneyToVol(amount);

            String[] attrid = {"pcsSrvTotUsedQuota", "pcsSubSrvThreshQuota"};
            Attributes attr = ctx.getAttributes(serviceDn, attrid);


            String quotatotalstr = (String) attr.get("pcsSubSrvThreshQuota").get();

            double topup = Double.parseDouble(bytes) + Double.parseDouble(quotatotalstr);
            BigDecimal bigDecimal = new BigDecimal(topup);
            // Prepare the modification
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
                    new BasicAttribute("pcsSubSrvThreshQuota", bigDecimal.toPlainString()));

            // Perform the update
            ctx.modifyAttributes(serviceDn, mods);
            logger.info("Credit account " + account + " with " + bytes + " bytes");

        } catch (NamingException e) {
            //e.printStackTrace();
            //System.out.println("Failed to insert entry.");
        }


        return true;
    }

    @Override
//    public boolean createAccount(String account) {
    public boolean createAccount(String account) {

        try {
            // 0. Create the subscriber entry
            Attributes subscriberAttrs = new BasicAttributes();
            subscriberAttrs.put("objectClass", "subscriber");
            subscriberAttrs.put("ds", "SUBSCRIBER");
            subscriberAttrs.put("o", "DEFAULT");
            subscriberAttrs.put("uid", account);
            // Add the subscriber entry
            addEntry("uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB", subscriberAttrs);


            Attributes subdataAttrs = new BasicAttributes();
            subdataAttrs.put("objectClass", "subdataElement");
            subdataAttrs.put("subdata", "services");
            addEntry("subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB", subdataAttrs);

            // 1. Add HLR subTemplate
            Attributes hlrAttrs = new BasicAttributes();
            hlrAttrs.put("objectClass", "subTemplate");
            hlrAttrs.put("ds", "hlr");
            hlrAttrs.put("o", "DEFAULT");
            addEntry("ds=hlr,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB", hlrAttrs);


            // 2. Add PCS subTemplate
            BasicAttributes pcsAttrs = new BasicAttributes();
            pcsAttrs.put("objectClass", "subTemplate");
            pcsAttrs.put("ds", "pcs");
            pcsAttrs.put("o", "DEFAULT");
            addEntry("ds=pcs,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB", pcsAttrs);

            // 3. Add IMSI Info
            Attributes imsiAttrs = new BasicAttributes();
            imsiAttrs.put("objectClass", "imsiInfo");
            imsiAttrs.put("ds", "hlr");
            imsiAttrs.put("imsi", account);
            imsiAttrs.put("o", "DEFAULT");
            addEntry("imsi=" + account + ",ds=hlr,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB", imsiAttrs);

            // 4. Add HLR Organization
            Attributes hlrOrgAttrs = new BasicAttributes();
            Attribute objectClass = new BasicAttribute("objectClass");
            objectClass.add(0, "dcObject");
            objectClass.add(1, "organization");
            hlrOrgAttrs.put(objectClass);
            hlrOrgAttrs.put("dc", "hlr");
            hlrOrgAttrs.put("o", "hlr");
            addEntry("o=hlr,imsi=" + account + ",ds=hlr,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB", hlrOrgAttrs);


            //  Add PDP Contexts (1, 2, and 3)
/* 
            Attributes pdpContextAttrs = new BasicAttributes();
            Attribute objectClassPdp=new BasicAttribute("objectClass");
            objectClassPdp.add(0, "pdpContext");
            objectClassPdp.add(1, "top");
            pdpContextAttrs.put(objectClassPdp);
    
            pdpContextAttrs.put("ds", "pcs");
            pdpContextAttrs.put("imsi", account);
            pdpContextAttrs.put("pdpContextId", "1");
            pdpContextAttrs.put("subdata", "pcsSubInfo");
            pdpContextAttrs.put("uid", account);
            pdpContextAttrs.put("accPointName", "net.hotm");
            pdpContextAttrs.put("pdpType", "1");
            pdpContextAttrs.put("refqOfServName", "phoneQoS");
            pdpContextAttrs.put("vplmnAllowed", "TRUE");

            //addEntry("pdpContextId=1,o=hlr,imsi="+account+",ds=hlr,subdata=services,uid="+account+",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB", pdpContextAttrs);
*/
            addPdpContext(account, "1", "net.hotm", "phoneQoS", true);
            addPdpContext(account, "2", "*", "wildQoS", false);
            addPdpContext(account, "3", "net.hotm", "mmsQoS", false);

            // 6. Add Profile Structure
            BasicAttributes profileAttrs = new BasicAttributes();
            profileAttrs.put("objectClass", "subdataElement");
            profileAttrs.put("subdata", "Profile");
            addEntry("subdata=Profile,ds=pcs,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB", profileAttrs);

            // 7. Add PCS Profile
            BasicAttributes pcsProfAttrs = new BasicAttributes();
            pcsProfAttrs.put("objectClass", "subdataElement");
            pcsProfAttrs.put("subdata", "pcsProf");
            addEntry("subdata=pcsProf,subdata=Profile,ds=pcs,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB", pcsProfAttrs);

            // 8. Add PCS Service Element
            addPcsServiceElement(account);

            // 9. Add PCS Subscriber Info
            addPcsSubscriberInfo(account);

        } catch (NamingException e) {
            e.printStackTrace();
            System.out.println("Failed to insert entry.");
        }

        return true;
    }

    @Override
    public boolean deleteAccount(String account) {
        try {
            // 9. Del PCS Subscriber Info
            String subInfoDn = "subdata=pcsSubInfo,subdata=pcsProf,subdata=Profile,ds=pcs,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";
            delEntry(subInfoDn);
        } catch (NamingException e) {
        }
        try {
            // 8. Del PCS Service Element
            String serviceDn = "pcsServiceId=200,subdata=pcsProf,subdata=Profile,ds=pcs,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";
            delEntry(serviceDn);
        } catch (NamingException e) {
        }
        try {
            // 7. Add PCS Profile
            String pcsProfile = "subdata=pcsProf,subdata=Profile,ds=pcs,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";
            delEntry(pcsProfile);
        } catch (NamingException e) {
        }
        try {
            // 6. Del Profile Structure
            String profileAttrs = "subdata=Profile,ds=pcs,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";
            delEntry(profileAttrs);
        } catch (NamingException e) {
        }
        try {
            //  Del PDP Contexts (1, 2, and 3)
            for (int id = 1; id <= 3; id++) {
                String pdpDn = String.format("pdpContextId=%s,o=hlr,imsi=" + account + ",ds=hlr,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB", id);
                delEntry(pdpDn);
            }
        } catch (NamingException e) {
        }
        try {
            // 4. Del HLR Organization
            String hlrOrgAttrs = "o=hlr,imsi=" + account + ",ds=hlr,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";
            delEntry(hlrOrgAttrs);
        } catch (NamingException e) {
        }
        try {
            // 3. Del IMSI Info
            String imsiAttrs = "imsi=" + account + ",ds=hlr,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";
            delEntry(imsiAttrs);
        } catch (NamingException e) {
        }
        try {

            // 2. Del PCS subTemplate
            String pcsAttrs = "ds=pcs,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";
            delEntry(pcsAttrs);
        } catch (NamingException e) {
        }
        try {
            // 1. Del HLR subTemplate
            String hlrAttrs = "ds=hlr,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";
            delEntry(hlrAttrs);
        } catch (NamingException e) {
        }
        try {
            // 0. Del the subscriber entry
            String subdataAttrs = "subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";
            delEntry(subdataAttrs);
        } catch (NamingException e) {
        }
        try {
            // -1. Del the subscriber entry
            String subscriberAttrs = "uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";
            delEntry(subscriberAttrs);
        } catch (NamingException e) {
        }

        return true;

    }


    public void addEntry(String dn, Attributes attrs) throws NamingException {
        try {
            ctx.createSubcontext(dn, attrs);
            System.out.println("Added entry: " + dn);
        } catch (NamingException e) {
            System.err.println("Failed to add entry: " + dn);
            throw e;
        }
    }

    public void delEntry(String dn) throws NamingException {
        try {
            ctx.destroySubcontext(dn);
            System.out.println("Deleted entry: " + dn);
        } catch (NamingException e) {
            System.err.println("Failed to del entry: " + dn);
            throw e;
        }
    }


    private void addPdpContext(String account, String id, String apn, String qosName, boolean vplmnAllowed) throws NamingException {

        Attributes pdpContextAttrs = new BasicAttributes();
        Attribute objectClassPdp = new BasicAttribute("objectClass");
        objectClassPdp.add(0, "pdpContext");
        objectClassPdp.add(1, "top");
        pdpContextAttrs.put(objectClassPdp);

        pdpContextAttrs.put("ds", "pcs");
        pdpContextAttrs.put("imsi", account);
        pdpContextAttrs.put("subdata", "pcsSubInfo");
        pdpContextAttrs.put("uid", account);
        pdpContextAttrs.put("accPointName", apn);
        pdpContextAttrs.put("pdpType", "1");
        pdpContextAttrs.put("refqOfServName", qosName);
        pdpContextAttrs.put("vplmnAllowed", "TRUE");

        String pdpDn = String.format("pdpContextId=%s,o=hlr,imsi=" + account + ",ds=hlr,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB", id);

        addEntry(pdpDn, pdpContextAttrs);

    }


    private void addPcsServiceElement(String account) throws NamingException {
        BasicAttributes serviceAttrs = new BasicAttributes();
        serviceAttrs.put("objectClass", "pcsServiceElement");
        // Add all attributes for PCS Service Element
        serviceAttrs.put("ds", "pcs");
        serviceAttrs.put("o", "DEFAULT");
        serviceAttrs.put("pcsServiceId", "200"); //TODO: Colocar aqui rootTL


        serviceAttrs.put("uid", account);
        serviceAttrs.put("subdata", "pcsProf");
        serviceAttrs.put("pcsServiceState", "TRUE");
        serviceAttrs.put("pcsSrvTotUsedQuota", "0");
        serviceAttrs.put("pcsSrvUsedULQuota", "0");
        serviceAttrs.put("pcsSrvUsedDLQuota", "0");
        serviceAttrs.put("pcsSrvUsedTime", "0");
        serviceAttrs.put("pcsServiceBillingStartDate", "20250101000000Z");
        serviceAttrs.put("pcsServiceAccCharge", "0");
        serviceAttrs.put("pcsServiceBillResetType", "duration");
        serviceAttrs.put("pcsServiceBillingResetDuration", "1000");
        serviceAttrs.put("pcsSubSrvThreshQuota", "0");  //TODO colocar quota  1073741824 ->1GB
        serviceAttrs.put("pcsSubSrvDLThreshQuota", "0");
        serviceAttrs.put("pcsServiceElementName", "30GB");
        serviceAttrs.put("pcsServiceElementCyclic", "TRUE");
        serviceAttrs.put("pcsSubSrvActionStatus", "0");
        serviceAttrs.put("pcsSubSrvThreshFutureQuota", "2");

        String serviceDn = "pcsServiceId=200,subdata=pcsProf,subdata=Profile,ds=pcs,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";
        addEntry(serviceDn, serviceAttrs);
    }


    private void addPcsSubscriberInfo(String account) throws NamingException {
        BasicAttributes subInfoAttrs = new BasicAttributes();
        subInfoAttrs.put("objectClass", "pcsSubscriberInfo");
        // Add all attributes for PCS Subscriber Info
        subInfoAttrs.put("ds", "pcs");
        subInfoAttrs.put("o", "DEFAULT");
        subInfoAttrs.put("subdata", "pcsSubInfo");
        // ... Add other attributes as needed

        subInfoAttrs.put("uid", account);
        subInfoAttrs.put("ds", "pcs");
        subInfoAttrs.put("subdata", "pcsSubInfo");
        subInfoAttrs.put("pcsPricingPlan", "phone");
        subInfoAttrs.put("refPcsQosProfName", "phoneQoS");
        subInfoAttrs.put("pcsIsHttpFirstHomeUse", "FALSE");
        subInfoAttrs.put("pcsIsHttpFirstRoamingUse", "FALSE");
        subInfoAttrs.put("pcsHomeLocation", "HOME");


        subInfoAttrs.put("pcsUserNotification", "0");
        subInfoAttrs.put("pcsUplinkThresholdQuota", "0");
        subInfoAttrs.put("pcsDownlinkThresholdQuota", "0");
        subInfoAttrs.put("pcsUsedQuota", "0");
        subInfoAttrs.put("pcsUsedUplinkQuota", "0");

        subInfoAttrs.put("pcsUsedDownlinkQuota", "0");
        subInfoAttrs.put("pcsSubscriberUsedTime", "0");
        subInfoAttrs.put("pcsAccumulatedCharge", "0");
        subInfoAttrs.put("pcsConsumptionLimit", "0");
        subInfoAttrs.put("pcsNotificationThreshold", "0");

        subInfoAttrs.put("pcsSubscriberCategory", "THROTTLE");
        subInfoAttrs.put("pcsBspAccumulatedCharge", "0");
        subInfoAttrs.put("pcsSmsNotificationsMsisdn", "97456456");
        subInfoAttrs.put("pcsSubscriberActionStatus", "0");


        String subInfoDn = "subdata=pcsSubInfo,subdata=pcsProf,subdata=Profile,ds=pcs,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";
        addEntry(subInfoDn, subInfoAttrs);
    }


    @Override
    public double getSaldo(String account) {
        double saldo = 0;
        double consumido = 0;
        double quotatotal = 0;
        String consumidostr = "";
        String quotatotalstr = "";

        try {
            String serviceDn = "pcsServiceId=200,subdata=pcsProf,subdata=Profile,ds=pcs,subdata=services,uid=" + account + ",ds=SUBSCRIBER,o=DEFAULT,dc=C-NTDB";

            String[] attrid = {"pcsSrvTotUsedQuota", "pcsSubSrvThreshQuota"};
            Attributes attr = ctx.getAttributes(serviceDn, attrid);

            consumidostr = (String) attr.get("pcsSrvTotUsedQuota").get();
            quotatotalstr = (String) attr.get("pcsSubSrvThreshQuota").get();


            consumido = Double.valueOf(consumidostr);
            quotatotal = Double.valueOf(quotatotalstr);
            saldo = quotatotal - consumido;
            logger.info("LDAP Consumido = {" + consumidostr + "} Quota total = {" + quotatotalstr + "}");
            if (saldo < 0) {
                System.out.println("Saldo menor que zero");
            }

        } catch (NamingException e) {
            e.printStackTrace();
            System.out.println("Failed to read entry.");
        }

        return saldo;
    }


}
