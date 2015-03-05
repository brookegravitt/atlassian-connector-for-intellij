CREATE OR REPLACE TEMPORARY VIEW monthly_usage_stats
AS
    SELECT
        first_month,
        auid,
        sum(coalesce(a, 0)) as asum,
        sum(coalesce(i, 0)) as isum,
        sum(coalesce(b, 0)) as bsum,
        sum(coalesce(r, 0)) as rsum
    FROM
        (
            SELECT
                auid,
                blogtime, 
                a,
                i,
                b,
                r,
                first_month
            FROM
                (
                    SELECT
                        uid as auid,
                        logtime as alogtime,
                        date_trunc('month', MIN(logtime)) AS first_month
                    FROM
                        downloadsummary
                    GROUP BY
                        auid, logtime
                    having 
                        sum(crucibleservers) + sum(jiraservers) + sum(bambooservers) > 0
                    ORDER BY
                        auid
                ) a
            JOIN
                (
                    SELECT
                        uid,
                        logtime as blogtime,
                        a,
                        i,
                        b,
                        r
                    FROM
                        downloadsummary
                    group by
                        uid, logtime, a, i, b, r
                    having 
                       sum(crucibleservers) + sum(jiraservers) + sum(bambooservers) > 0
                    order by
                       uid
                ) b
            ON
                auid = b.uid
            AND date_trunc('month', blogtime) = first_month
            group by
                auid, a.first_month, blogtime, b.a, b.i, b.b, b.r
            ORDER BY
                auid
        )
        first_users_with_any_atlassian
    GROUP BY
        first_month, auid;

create or replace temporary view optinsvsoptouts 
as 
    select
        month,
        optins,
        optouts,
        round(optins/(optins+optouts+0.0000001), 2) as optinoutratio
    from
        (
            select
                date_trunc('month', date) as month,
                sum(optin) as optins,
                sum(optout) as optouts
            from 
                optactions
            group by 
                month
        ) foo
    order by 
        month;

--select * from monthly_usage_stats group by first_month, auid, asum, isum, bsum, rsum;

create or replace temporary view pl_usagestats 
as 

SELECT
    to_char(c.month, 'YYYY-MM') as "Period",
    used_any + not_used_any as "New Users (Logged)",
    used_any as "# Used Any Feature (Logged)",
    round(used_any / optinoutratio, 0) as "# Used Any Feature (Total) - estimated",
    --not_used_any as "# Did Not Use Any Feature (Logged)",
    to_char(100 * used_any::FLOAT / (used_any + not_used_any), '990D9%') as "% Used Any Feature",
    to_char(100 * used_jira::FLOAT / (used_any + not_used_any), '990D9%') as "% Used JIRA Feature",
    to_char(100 * used_bamboo::FLOAT / (used_any + not_used_any), '990D9%') as "% Used Bamboo Feature",
    to_char(100 * used_cru::FLOAT / (used_any + not_used_any), '990D9%') as "% Used Crucible Feature"
FROM
    (
        select
            first_month,
            sum(case when rsum > 0 or asum > 0 or bsum > 0 or isum > 0 then 1 else 0 end) as used_any,
            sum(case when rsum = 0 and asum = 0 and bsum = 0 and isum = 0 then 1 else 0 end) as not_used_any,
            sum(case when isum > 0 or asum > 0 then 1 else 0 end) as used_jira,
            sum(case when bsum > 0 then 1 else 0 end) as used_bamboo,
            sum(case when rsum > 0 then 1 else 0 end) as used_cru
        FROM
            monthly_usage_stats
        GROUP BY
            first_month
    ) a
JOIN optinsvsoptouts c
ON
    c.month = a.first_month
ORDER BY
    "Period";

drop table if exists pl_feature_usage_new;
select * into pl_feature_usage_new from pl_usagestats       
