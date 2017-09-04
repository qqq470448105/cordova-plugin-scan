#import "ScanPlugin.h"

@implementation ScanPlugin

//扫一扫
- (void)scan:(CDVInvokedUrlCommand *)command
{
    self.callid_code = [[NSString alloc] initWithString:[NSString stringWithFormat:@"%@",command.callbackId]];
    Scan_VC *scanCtl = [[Scan_VC alloc]init];
    scanCtl.delegate = self;
    [self.viewController.navigationController pushViewController:scanCtl animated:YES];
}

#pragma mark 扫码结果代理

//扫码成功
-(void)scanSuccess:(NSString *)messStr{
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:messStr];
    //回调js
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callid_code];
}

//扫码失败
-(void)scanfalse:(NSString *)messStr
{
    CDVPluginResult* pluginResult = nil;
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:messStr];
    //回调js
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.callid_code];
}
@end
