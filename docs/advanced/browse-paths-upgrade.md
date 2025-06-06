# Browse Paths Upgrade (August 2022)

## Background

Up to this point, there's been a historical constraint on all entity browse paths. Namely, each browse path has been
required to end with a path component that represents "simple name" for an entity. For example, a Browse Path for a
Snowflake Table called "test_table" may look something like this:

```
/prod/snowflake/warehouse1/db1/test_table
```

In the UI, we artificially truncate the final path component when you are browsing the Entity hierarchy, so your browse experience
would be:

`prod` > `snowflake` > `warehouse1`> `db1` > `Click Entity`

As you can see, the final path component `test_table` is effectively ignored. It could have any value, and we would still ignore
it in the UI. This behavior serves as a workaround to the historical requirement that all browse paths end with a simple name.

This data constraint stands in opposition the original intention of Browse Paths: to provide a simple mechanism for organizing
assets into a hierarchical folder structure. For this reason, we've changed the semantics of Browse Paths to better align with the original intention.
Going forward, you will not be required to provide a final component detailing the "name". Instead, you will be able to provide a simpler path that
omits this final component:

```
/prod/snowflake/warehouse1/db1
```

and the browse experience from the UI will continue to work as you would expect:

`prod` > `snowflake` > `warehouse1`> `db1` > `Click Entity`.

With this change comes a fix to a longstanding bug where multiple browse paths could not be attached to a single URN. Going forward,
we will support producing multiple browse paths for the same entity, and allow you to traverse via multiple paths. For example

```python
browse_path = BrowsePathsClass(
    paths=["/powerbi/my/custom/path", "/my/other/custom/path"]
)
return MetadataChangeProposalWrapper(
    entityType="dataset",
    changeType="UPSERT",
    entityUrn="urn:li:dataset:(urn:li:dataPlatform:custom,MyFileName,PROD),
    aspectName="browsePaths",
    aspect=browse_path,
)
```

_Using the Python Emitter SDK to produce multiple Browse Paths for the same entity_

We've received multiple bug reports, such as [this issue](https://github.com/datahub-project/datahub/issues/5525), and requests to address these issues with Browse, and thus are deciding
to do it now before more workarounds are created.

## What this means for you

Once you upgrade to DataHub `v0.8.45` you will immediately notice that traversing your Browse Path hierarchy will require
one extra click to find the entity. This is because we are correctly displaying the FULL browse path, including the simple name mentioned above.

There will be 2 ways to upgrade to the new browse path format. Depending on your ingestion sources, you may want to use one or both:

1. Migrate default browse paths to the new format by restarting DataHub
2. Upgrade your version of the `datahub` CLI to push new browse path format (version `v0.8.45`)

Each step will be discussed in detail below.

### 1. Migrating default browse paths to the new format

To migrate those Browse Paths that are generated by DataHub by default (when no path is provided), simply restart the `datahub-gms` container / pod with a single
additional environment variable:

```
UPGRADE_DEFAULT_BROWSE_PATHS_ENABLED=true
```

And restart the `datahub-gms` instance. This will cause GMS to perform a boot-time migration of all your existing Browse Paths
to the new format, removing the unnecessarily name component at the very end.

If the migration is successful, you'll see the following in your GMS logs:

```
18:58:17.414 [main] INFO c.l.m.b.s.UpgradeDefaultBrowsePathsStep:60 - Successfully upgraded all browse paths!
```

After this one-time migration is complete, you should be able to navigate the Browse hierarchy exactly as you did previously.

> Note that certain ingestion sources actively produce their own Browse Paths, which overrides the default path
> computed by DataHub.
>
> In these cases, getting the updated Browse Path will require re-running your ingestion process with the updated
> version of the connector. This is discussed in more detail in the next section.

### 2. Upgrading the `datahub` CLI to push new browse paths

If you are actively ingesting metadata from one or more of following sources

1. Sagemaker
2. Looker / LookML
3. Feast
4. Kafka
5. Mode
6. PowerBi
7. Pulsar
8. Tableau
9. Business Glossary

You will need to upgrade the DataHub CLI to >= `v0.8.45` and re-run metadata ingestion. This will generate the new browse path format
and overwrite the existing paths for entities that were extracted from these sources.

### If you are producing custom Browse Paths

If you've decided to produce your own custom Browse Paths to organize your assets (e.g. via the Python Emitter SDK), you'll want to change the code to produce those paths
to truncate the final path component. For example, if you were previously emitting a browse path like this:

```
"my/custom/browse/path/suffix"
```

You can simply remove the final "suffix" piece:

```
"my/custom/browse/path"
```

Your users will be able to find the entity by traversing through these folders in the UI:

`my` > `custom` > `browse`> `path` > `Click Entity`.

> Note that if you are using the Browse Path Transformer you _will_ be impacted in the same way. It is recommended that you revisit the
> paths that you are producing, and update them to the new format.

## Support

The DataHub team will be on standby to assist you in your migration. Please
join [#release-0_8_0](https://datahubspace.slack.com/archives/C0244FHMHJQ) channel and reach out to us if you find
trouble with the upgrade or have feedback on the process. We will work closely to make sure you can continue to operate
DataHub smoothly.
