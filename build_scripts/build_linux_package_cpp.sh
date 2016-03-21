#!/bin/bash
# This script collects all the pieces to make the SDK for the cpp builds
# Based on the equivalent Windows Batch Script
# Then further based on https://build.allseenalliance.org/baseservices/job/CoreMaster_linux-base-services_Master/configure

SDK_TOP_DIR=alljoyn-$SERVICE-service-framework-${BUILD_VERSION}-linux-$CPU-sdk-$VARIANT
ZIP_FILE=$SDK_TOP_DIR.zip
SDK_DIR=$SDK_TOP_DIR/alljoyn-linux-$CPU/alljoyn-$SERVICE-${BUILD_VERSION}-$VARIANT/cpp

## Generate Doxygen docs
DOXYFILE=Doxygen_html
# where to run the doxygen command
pushd $BUILD_ROOT/$SERVICE/cpp/docs
rm -f doxy.out
doxygen $DOXYFILE 2> doxy.out >> /dev/null
popd

rm -rf artifacts
mkdir -p artifacts

cp -r $BUILD_ROOT/$SERVICE/build/linux/$CPU/$VARIANT/dist/* artifacts

rm -rf $SDK_TOP_DIR
mkdir -p $SDK_DIR/inc/alljoyn/$SERVICE 
mkdir -p $SDK_DIR/inc/alljoyn/services_common
for a in lib bin docs samples; do
    mkdir -p $SDK_DIR/$a
done

## Preparing the ZIP
cp -r $BUILD_ROOT/$SERVICE/cpp/samples/*                         $SDK_DIR/samples
cp -r $BUILD_ROOT/$SERVICE/cpp/docs/*                            $SDK_DIR/docs
cp -r artifacts/$SERVICE/bin/*                                     $SDK_DIR/bin
cp -r artifacts/services_common/lib/*                           $SDK_DIR/lib
cp -r artifacts/services_common/inc/alljoyn/services_common/*   $SDK_DIR/inc/alljoyn/services_common
cp -r core/alljoyn/build/linux/$CPU/$VARIANT/dist/cpp/lib/*           $SDK_DIR/lib
if [ "$SERVICE" == "sample_apps" ]; then
    #cp -r artifacts/controlpanel/lib/*                                     $SDK_DIR/lib
    cp -r artifacts/notification/lib/*                                     $SDK_DIR/lib
    #cp -r artifacts/onboarding/lib/*                                     $SDK_DIR/lib
else
    cp -r artifacts/$SERVICE/lib/*                                     $SDK_DIR/lib
    cp -r artifacts/$SERVICE/inc/alljoyn/$SERVICE/*                       $SDK_DIR/inc/alljoyn/$SERVICE
fi

rm -rf $ZIP_FILE
zip -r $ZIP_FILE $SDK_TOP_DIR
md5sum $ZIP_FILE > md5-$SDK_TOP_DIR.txt
