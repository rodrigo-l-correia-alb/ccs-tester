package pt.ptinovacao;

import org.json.JSONException;
import org.json.JSONObject;
import pt.ptinovacao.InterfaceHTTP.AgreementType;

import java.util.List;

public class Service {

    private String serviceContexId;

    private String usagePatternName;

    private UsagePattern usagePattern;

    public Service(JSONObject service) {
        this.serviceContexId = service.has("serviceContextId")
                ? service.getString("serviceContextId")
                : "topup@ptinovacao.pt";

        try {
            this.usagePattern = new UsagePattern(service.getJSONObject("usage_pattern"));
        } catch (JSONException e) {
            this.usagePatternName = service.getString("usage_pattern");
        }
    }

    public Service() {
    }

    private void _linkPattern(String name, UsagePattern usage_pattern) {
        if (usage_pattern.getName() != null && name == usage_pattern.getName()) {
            this.usagePattern = usage_pattern;
        }
    }

    public void linkPattern(List<UsagePattern> usage_patterns) {
        usage_patterns.forEach(usage_pattern -> _linkPattern(this.usagePatternName, usage_pattern));

    }

    public AgreementType getServiceContextId() {
        switch (this.serviceContexId) {
            case "message@huawei.com": {
                return AgreementType.SMS;
            }
            case "7.APC@telecom.pt": {
                return AgreementType.VOICE;
            }
            default:
                return AgreementType.DATA;
        }
    }

    public String getServiceContexId() {
        return serviceContexId;
    }

    public UsagePattern getUsagePattern() {
        return usagePattern;
    }

    public String getUsagePatternName() {
        return usagePatternName;
    }

    public boolean hasServiceContextId() {
        return this.serviceContexId != null;
    }

    @Override
    public String toString() {
        return "Service{" +
                "serviceContexId='" + serviceContexId + '\'' +
                ", usagePatternName='" + usagePatternName + '\'' +
                ", usagePattern=" + usagePattern +
                '}';
    }

}
