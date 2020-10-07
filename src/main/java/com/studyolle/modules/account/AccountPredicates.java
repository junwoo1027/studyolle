package com.studyolle.modules.account;

import com.querydsl.core.types.Predicate;
import com.studyolle.modules.tag.Tag;
import com.studyolle.modules.zone.Zone;

import java.util.Set;

import static com.studyolle.modules.account.QAccount.*;

public class AccountPredicates {

    public static Predicate findByTagsAndZones(Set<Tag> tags, Set<Zone> zones) {
        return account.zones.any().in(zones).and(account.tags.any().in(tags));
    }
}
