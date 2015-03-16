package me.itzg.mccy.services;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.itzg.mccy.MccyConstants;
import me.itzg.mccy.model.DistributionType;
import me.itzg.mccy.model.ForgePromotionsBlob;
import me.itzg.mccy.model.ForgeVersion;
import me.itzg.mccy.model.ReleaseType;
import me.itzg.mccy.model.VanillaVersionsBlob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Geoff Bourne
 * @since 3/15/2015
 */
@Service
public class VersionsService {

    @Autowired @Qualifier("json")
    private RestTemplate restTemplate;

    public Collection<String> getVanillaVersions(ReleaseType releaseType) {
        final VanillaVersionsBlob versionsBlob = restTemplate.getForObject(MccyConstants.VERSIONS_VANILLA_JSON, VanillaVersionsBlob.class);
        final String expectedType = releaseType == ReleaseType.STABLE ? "release" : "snapshot";

        final Collection<VanillaVersionsBlob.Version> filteredVersions = Collections2.filter(versionsBlob.versions, new Predicate<VanillaVersionsBlob.Version>() {
            @Override
            public boolean apply(VanillaVersionsBlob.Version input) {
                return expectedType.equals(input.type);
            }
        });

        return Collections2.transform(filteredVersions, new Function<VanillaVersionsBlob.Version, String>() {
            @Override
            public String apply(VanillaVersionsBlob.Version input) {
                return input.id;
            }
        });
    }

    public Collection<ForgeVersion> getForgeVersions(ReleaseType releaseType) {
        final ForgePromotionsBlob promotionsBlob = restTemplate.getForObject(MccyConstants.VERSIONS_FORGE_JSON, ForgePromotionsBlob.class);
        final String expectedType = releaseType == ReleaseType.STABLE ? "recommended" : "latest";

        final Map<String, ForgeVersion> transformed = Maps.transformEntries(promotionsBlob.promos, new Maps.EntryTransformer<String, String, ForgeVersion>() {
            @Override
            public ForgeVersion transformEntry(String key, String value) {
                final String[] parts = key.split("-", 2);
                if (parts.length >= 2) {
                    ForgeVersion forgeVersion = new ForgeVersion();
                    forgeVersion.setVanillaVersion(parts[0]);
                    forgeVersion.setForgeVersion(value);
                    forgeVersion.setType(parts[1]);
                    return forgeVersion;
                } else {
                    return null;
                }
            }
        });

        final Map<String, ForgeVersion> filtered = Maps.filterValues(transformed, new Predicate<ForgeVersion>() {
            @Override
            public boolean apply(ForgeVersion input) {
                return input != null && expectedType.equals(input.getType());
            }
        });

        return filtered.values();
    }
}
