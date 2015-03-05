create or replace temporary view users_running_stats as
    SELECT
        month,
        COUNT (DISTINCT uid) AS users_reported_before
    FROM
        (
            SELECT
                a.month AS month,
                uid
            FROM
                (
                    SELECT DISTINCT
                        date_trunc('month', logtime) AS month
                    FROM
                        downloadsummary
                    WHERE
                        logtime <= now()
                ) a
            JOIN
                (
                    SELECT
                        uid,
                        date_trunc('month', logtime) AS month
                    FROM
                        downloadsummary
                ) b
            ON
                b.month <= a.month
            ORDER BY
                month,
                uid
        ) c
    GROUP BY
        month
    ORDER BY
        month;

create or replace temporary view userlogins as 
    select
        month,
        logins
    from
        (
            select
                date_trunc('month', logtime) as month,
                count(distinct uid) as logins
            from 
                downloadsummary 
            group by 
                month
        ) foo
    order by 
        month;

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

create or replace temporary view pl_usagestats2 as
select 
    to_char(uaf.month, 'YYYY-MM') as "Period",
    users_reported_before as "Existing Users (pinged ever)",
    logins as "Active Users this month",
    used_any + not_used_any as "Active Users with Servers",
    used_any as "# Used Any Feature (Logged)",
    round(used_any / optinoutratio, 0) as "# Used Any Feature (Total) - estimated",
    --not_used_any as "# Did Not Use Any Feature (Logged)",
    to_char(100 * used_any::FLOAT / (used_any + not_used_any), '990D9%') as "% Used Any Feature",
    to_char(100 * used_jira::FLOAT / (used_any + not_used_any), '990D9%') as "% Used JIRA Feature",
    to_char(100 * used_bamboo::FLOAT / (used_any + not_used_any), '990D9%') as "% Used Bamboo Feature",
    to_char(100 * used_cru::FLOAT / (used_any + not_used_any), '990D9%') as "% Used Crucible Feature"
from 
    (
        select
            month,
            sum(case when rsum > 0 or asum > 0 or bsum > 0 or isum > 0 then 1 else 0 end) as used_any,
            sum(case when rsum = 0 and asum = 0 and bsum = 0 and isum = 0 then 1 else 0 end) as not_used_any,
            sum(case when isum > 0 or asum > 0 then 1 else 0 end) as used_jira,
            sum(case when bsum > 0 then 1 else 0 end) as used_bamboo,
            sum(case when rsum > 0 then 1 else 0 end) as used_cru
        from 
            (
                select
                    date_trunc('month', logtime) as month, 
                    uid,
                    sum(coalesce(a, 0)) as asum,
                    sum(coalesce(i, 0)) as isum,
                    sum(coalesce(b, 0)) as bsum,
                    sum(coalesce(r, 0)) as rsum
                from 
                    downloadsummary
                where 
 --                   logtime >= date_trunc('month', now() - interval '52 weeks')
 --               and 
                    uid is not null
                group by 
                    month, uid
                having 
                    sum(crucibleservers) + sum(jiraservers) + sum(bambooservers) > 0
            ) usage 
        group by 
            month
    ) uaf
join
    userlogins logins
on
    logins.month = uaf.month
join 
    optinsvsoptouts oioo
on 
    oioo.month = uaf.month
JOIN
    users_running_stats urs
ON
    uaf.month = urs.month

group by 
    uaf.month, uaf.used_any, uaf.not_used_any, uaf.used_jira, uaf.used_bamboo, uaf.used_cru, logins.month, logins.logins, oioo.optinoutratio, urs.users_reported_before 
order by 
    uaf.month;

drop table if exists pl_feature_usage_existing;
select * into pl_feature_usage_existing from pl_usagestats2  